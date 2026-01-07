import logging
import json
import time
import requests
import re
from typing import Dict, Any, Optional, List, AsyncGenerator
from app.core.config import config

logger = logging.getLogger(__name__)


class LLMService:
    def __init__(self):
        self.api_key = config.qwen.api_key
        self.model = config.qwen.model
        self.base_url = config.qwen.base_url
        self.max_tokens = config.qwen.max_tokens
        self.temperature = config.qwen.temperature
        self.top_p = config.qwen.top_p
        self.timeout = config.qwen.timeout
        self.enable_response_format = config.qwen.enable_response_format
        self.prompts = config.prompts
        self.messages = config.messages
    
    def _parse_response(self, response: Dict[str, Any]) -> str:
        if "choices" in response and len(response["choices"]) > 0:
            return response["choices"][0]["message"]["content"]
        raise ValueError("Invalid response format")
    
    def _call_api(self, request_data: Dict[str, Any], stream: bool = True) -> Dict[str, Any]:
        import time as time_module
        start_time = time_module.time()
        
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        
        if stream:
            headers["Accept"] = "text/event-stream"
        
        url = f"{self.base_url}/chat/completions"
        
        request_data.setdefault("max_tokens", self.max_tokens)
        request_data.setdefault("temperature", self.temperature)
        request_data.setdefault("top_p", self.top_p)
        request_data.setdefault("stream", stream)
        
        if self.enable_response_format and not stream:
            request_data.setdefault("response_format", {"type": "json_object"})
        
        prompt_preview = ""
        if "messages" in request_data and len(request_data["messages"]) > 0:
            content = request_data["messages"][0].get("content", "")
            prompt_preview = content[:200] + "..." if len(content) > 200 else content
        
        logger.info(f"[LLM] API request: url={url}, model={request_data.get('model')}, stream={stream}, maxTokens={request_data.get('max_tokens')}, promptLength={len(prompt_preview)}")
        logger.debug(f"[LLM] Request prompt preview: {prompt_preview}")
        
        max_retries = 2
        last_exception = None
        
        for attempt in range(max_retries):
            try:
                response = requests.post(
                    url,
                    headers=headers,
                    json=request_data,
                    timeout=self.timeout,
                    stream=stream
                )
                
                request_duration = time_module.time() - start_time
                
                if response.status_code == 200:
                    if stream:
                        result = self._parse_stream_response(response)
                        total_duration = time_module.time() - start_time
                        content_length = len(result.get("choices", [{}])[0].get("message", {}).get("content", ""))
                        logger.info(f"[LLM] API response success: duration={total_duration:.2f}s, contentLength={content_length}, stream={stream}")
                        logger.debug(f"[LLM] Response content preview: {result.get('choices', [{}])[0].get('message', {}).get('content', '')[:200]}")
                        return result
                    else:
                        result = response.json()
                        total_duration = time_module.time() - start_time
                        content_length = len(result.get("choices", [{}])[0].get("message", {}).get("content", ""))
                        logger.info(f"[LLM] API response success: duration={total_duration:.2f}s, contentLength={content_length}, stream={stream}")
                        logger.debug(f"[LLM] Response content preview: {result.get('choices', [{}])[0].get('message', {}).get('content', '')[:200]}")
                        return result
                elif response.status_code == 401:
                    logger.error(f"[LLM] API authentication failed: status={response.status_code}, response={response.text[:200]}")
                    raise Exception(f"LLM API authentication failed: Invalid API key")
                elif response.status_code == 429:
                    logger.warning(f"[LLM] API rate limited: attempt={attempt + 1}/{max_retries}, response={response.text[:200]}")
                    if attempt < max_retries - 1:
                        time.sleep(2 ** attempt)
                        continue
                    raise Exception(f"LLM API rate limited: {response.text}")
                else:
                    logger.error(f"[LLM] API error: status={response.status_code}, response={response.text[:500]}")
                    raise Exception(f"LLM API error: {response.status_code}, {response.text}")
                    
            except requests.exceptions.Timeout as e:
                request_duration = time_module.time() - start_time
                logger.warning(f"[LLM] API timeout: attempt={attempt + 1}/{max_retries}, duration={request_duration:.2f}s, error={str(e)}")
                last_exception = e
                if attempt < max_retries - 1:
                    continue
                raise Exception(f"LLM API timeout after {max_retries} attempts: {str(e)}")
            except requests.exceptions.RequestException as e:
                request_duration = time_module.time() - start_time
                logger.error(f"[LLM] API request failed: attempt={attempt + 1}/{max_retries}, duration={request_duration:.2f}s, error={str(e)}")
                last_exception = e
                if attempt < max_retries - 1:
                    time.sleep(1)
                    continue
                raise Exception(f"LLM API request failed: {str(e)}")
        
        if last_exception:
            raise last_exception
        raise Exception("LLM API request failed after retries")
    
    def _parse_stream_response(self, response) -> Dict[str, Any]:
        full_content = ""
        chunk_count = 0
        try:
            for line in response.iter_lines(decode_unicode=True):
                if not line:
                    continue
                
                line_text = line.strip()
                if not line_text:
                    continue
                
                if line_text.startswith('data: '):
                    data_str = line_text[6:].strip()
                    if data_str == '[DONE]':
                        logger.debug(f"[LLM] Stream parsing completed: chunks={chunk_count}, totalLength={len(full_content)}")
                        break
                    if not data_str:
                        continue
                    
                    try:
                        data = json.loads(data_str)
                        if 'choices' in data and len(data['choices']) > 0:
                            delta = data['choices'][0].get('delta', {})
                            if 'content' in delta and delta['content']:
                                full_content += delta['content']
                                chunk_count += 1
                    except json.JSONDecodeError as e:
                        logger.debug(f"[LLM] Failed to parse SSE chunk: {data_str[:100]}, error: {e}")
                        continue
                elif line_text.startswith(':'):
                    continue
        except Exception as e:
            logger.error(f"[LLM] Error parsing stream response: chunks={chunk_count}, contentLength={len(full_content)}, error: {e}")
            if not full_content:
                raise
        
        logger.debug(f"[LLM] Stream response parsed: chunks={chunk_count}, finalLength={len(full_content)}")
        return {
            "choices": [{
                "message": {
                    "content": full_content
                }
            }]
        }
    
    async def _stream_api_response(self, request_data: Dict[str, Any]) -> AsyncGenerator[str, None]:
        import asyncio
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "Accept": "text/event-stream"
        }
        
        url = f"{self.base_url}/chat/completions"
        request_data.setdefault("max_tokens", self.max_tokens)
        request_data.setdefault("temperature", self.temperature)
        request_data.setdefault("top_p", self.top_p)
        request_data.setdefault("stream", True)
        
        logger.info(f"[LLM] Streaming API request: url={url}, model={request_data.get('model')}, stream=True")
        
        try:
            def make_request():
                return requests.post(
                    url,
                    headers=headers,
                    json=request_data,
                    timeout=self.timeout,
                    stream=True
                )
            
            loop = asyncio.get_event_loop()
            response = await loop.run_in_executor(None, make_request)
            
            if response.status_code != 200:
                error_msg = f"LLM API error: {response.status_code}, {response.text[:200]}"
                logger.error(f"[LLM] {error_msg}")
                yield error_msg
                return
            
            def iter_lines():
                for line in response.iter_lines(decode_unicode=True):
                    yield line
            
            for line in iter_lines():
                if not line:
                    continue
                
                line_text = line.strip()
                if not line_text or line_text.startswith(':'):
                    continue
                
                if line_text.startswith('data: '):
                    data_str = line_text[6:].strip()
                    if data_str == '[DONE]':
                        logger.debug(f"[LLM] Stream completed")
                        break
                    if not data_str:
                        continue
                    
                    try:
                        data = json.loads(data_str)
                        if 'choices' in data and len(data['choices']) > 0:
                            delta = data['choices'][0].get('delta', {})
                            if 'content' in delta and delta['content']:
                                yield delta['content']
                    except json.JSONDecodeError as e:
                        logger.debug(f"[LLM] Failed to parse SSE chunk: {data_str[:100]}, error: {e}")
                        continue
                        
        except Exception as e:
            logger.error(f"[LLM] Stream API error: {e}", exc_info=True)
            yield f"\n\n[error: {str(e)}]"
    
    def generate_career_roadmap(
        self,
        job_title: str,
        education: str,
        work_years: float,
        required_missing_skills: List[str],
        optional_missing_skills: List[str],
        matched_skills: List[Dict[str, Any]],
        all_job_skills: List[str]
    ) -> Optional[Dict[str, Any]]:
        logger.info(f"[LLM] Generating career roadmap: jobTitle={job_title}, education={education}, workYears={work_years}, requiredMissing={len(required_missing_skills)}, optionalMissing={len(optional_missing_skills)}")
        
        no_data_label = self.messages.labels.get("no_data", "None")
        required_skills_text = ", ".join(required_missing_skills) if required_missing_skills else no_data_label
        optional_skills_text = ", ".join(optional_missing_skills) if optional_missing_skills else no_data_label
        matched_skills_text = ", ".join([s.get("name", "") for s in matched_skills]) if matched_skills else no_data_label
        all_job_skills_text = ", ".join(all_job_skills) if all_job_skills else no_data_label
        
        num_phases = max(len(required_missing_skills), 3)
        
        system_message = self.prompts.career_roadmap.get("system", "")
        user_template = self.prompts.career_roadmap.get("user_template", "")
        
        prompt = user_template.format(
            job_title=job_title,
            education=education,
            work_years=work_years,
            required_missing=required_skills_text,
            optional_missing=optional_skills_text,
            matched_skills=matched_skills_text,
            all_job_skills=all_job_skills_text,
            num_phases=num_phases
        )
        
        prompt_length = len(prompt)
        logger.debug(f"[LLM] Career roadmap prompt: length={prompt_length}, numPhases={num_phases}")
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            logger.info(f"[LLM] Calling LLM API for career roadmap: model={self.model}, stream=False, responseFormat=json_object")
            response = self._call_api(request_data, stream=False)
            result = self._parse_response(response)
            logger.info(f"[LLM] Career roadmap received: responseLength={len(result)}")
            
            try:
                roadmap_data = json.loads(result)
                logger.info(f"[LLM] Career roadmap parsed successfully: techStacks={len(roadmap_data.get('technology_stacks', []))}, phases={len(roadmap_data.get('phase_roadmap', []))}, suggestions={len(roadmap_data.get('immediate_suggestions', []))}")
                return roadmap_data
            except json.JSONDecodeError as e:
                logger.error(f"[LLM] Failed to parse career roadmap JSON: {e}, rawResponse={result[:200]}")
                return None
                
        except Exception as e:
            logger.error(f"[LLM] Failed to generate career roadmap: {e}", exc_info=True)
            return None
    
    def generate_learning_plan_structured(
        self,
        required_missing_skills: List[str],
        optional_missing_skills: List[str],
        matched_skills: List[Dict[str, Any]],
        job_description: str
    ) -> Optional[Dict[str, Any]]:
        logger.info(f"[LLM] Generating structured learning plan: requiredMissing={len(required_missing_skills)}, optionalMissing={len(optional_missing_skills)}, matched={len(matched_skills)}")
        
        no_data_label = self.messages.labels.get("no_data", "None")
        required_skills_text = ", ".join(required_missing_skills) if required_missing_skills else no_data_label
        optional_skills_text = ", ".join(optional_missing_skills) if optional_missing_skills else no_data_label
        matched_skills_text = ", ".join([s.get("name", "") for s in matched_skills]) if matched_skills else no_data_label
        
        system_message = self.prompts.learning_plan_structured.get("system", "")
        user_template = self.prompts.learning_plan_structured.get("user_template", "")
        
        prompt = user_template.format(
            required_missing=required_skills_text,
            optional_missing=optional_skills_text,
            matched_skills=matched_skills_text,
            job_description=job_description
        )
        
        prompt_length = len(prompt)
        logger.debug(f"[LLM] Structured learning plan prompt: length={prompt_length}")
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            logger.info(f"[LLM] Calling LLM API for structured learning plan: model={self.model}, stream=False, responseFormat=json_object")
            response = self._call_api(request_data, stream=False)
            result = self._parse_response(response)
            logger.info(f"[LLM] Structured learning plan received: responseLength={len(result)}")
            
            try:
                learning_plan_data = json.loads(result)
                skills_count = len(learning_plan_data.get('skills', []))
                logger.info(f"[LLM] Structured learning plan parsed successfully: skillsCount={skills_count}")
                return learning_plan_data
            except json.JSONDecodeError as e:
                logger.error(f"[LLM] Failed to parse structured learning plan JSON: {e}, rawResponse={result[:200]}")
                return None
                
        except Exception as e:
            logger.error(f"[LLM] Failed to generate structured learning plan: {e}", exc_info=True)
            return None
    
    def generate_learning_plan(
        self,
        required_missing_skills: List[str],
        optional_missing_skills: List[str],
        matched_skills: List[Dict[str, Any]],
        job_description: str,
        job_requirements: str
    ) -> Optional[str]:
        logger.info(f"[LLM] Generating learning plan: requiredMissing={len(required_missing_skills)}, optionalMissing={len(optional_missing_skills)}, matched={len(matched_skills)}")
        
        no_data_label = self.messages.labels.get("no_data", "None")
        required_skills_text = ", ".join(required_missing_skills) if required_missing_skills else no_data_label
        optional_skills_text = ", ".join(optional_missing_skills) if optional_missing_skills else no_data_label
        matched_skills_text = ", ".join([s.get("name", "") for s in matched_skills]) if matched_skills else no_data_label
        
        system_message = self.prompts.learning_plan.get("system", "")
        user_template = self.prompts.learning_plan.get("user_template", "")
        
        prompt = user_template.format(
            required_skills=required_skills_text,
            optional_skills=optional_skills_text,
            matched_skills=matched_skills_text,
            job_description=job_description,
            job_requirements=job_requirements
        )
        
        prompt_length = len(prompt)
        logger.debug(f"[LLM] Learning plan prompt: length={prompt_length}, jobDescLength={len(job_description)}, jobReqLength={len(job_requirements)}")
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            logger.info(f"[LLM] Calling LLM API for learning plan: model={self.model}, stream=True")
            response = self._call_api(request_data)
            result = self._parse_response(response)
            logger.info(f"[LLM] Learning plan generated successfully: responseLength={len(result)}, preview={result[:100]}")
            return result
        except Exception as e:
            logger.error(f"[LLM] Failed to generate learning plan: {e}", exc_info=True)
            return None
    
    async def generate_learning_plan_stream(
        self,
        required_missing_skills: List[str],
        optional_missing_skills: List[str],
        matched_skills: List[Dict[str, Any]],
        job_description: str,
        job_requirements: str
    ) -> AsyncGenerator[str, None]:
        logger.info(f"[LLM] Generating learning plan stream: requiredMissing={len(required_missing_skills)}, optionalMissing={len(optional_missing_skills)}, matched={len(matched_skills)}")
        
        no_data_label = self.messages.labels.get("no_data", "None")
        required_skills_text = ", ".join(required_missing_skills) if required_missing_skills else no_data_label
        optional_skills_text = ", ".join(optional_missing_skills) if optional_missing_skills else no_data_label
        matched_skills_text = ", ".join([s.get("name", "") for s in matched_skills]) if matched_skills else no_data_label
        
        system_message = self.prompts.learning_plan.get("system", "")
        user_template = self.prompts.learning_plan.get("user_template", "")
        
        prompt = user_template.format(
            required_skills=required_skills_text,
            optional_skills=optional_skills_text,
            matched_skills=matched_skills_text,
            job_description=job_description,
            job_requirements=job_requirements
        )
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            async for chunk in self._stream_api_response(request_data):
                yield chunk
        except Exception as e:
            error_prefix = self.messages.stream.get("error_prefix", "Error: ")
            logger.error(f"[LLM] Failed to generate learning plan stream: {e}", exc_info=True)
            yield f"\n\n[{error_prefix}{str(e)}]"
    
    def generate_interview_prep(
        self,
        job_title: str,
        job_requirements: str,
        project_experiences: List[Dict[str, Any]],
        work_experiences: List[Dict[str, Any]],
        missing_skills: List[str]
    ) -> Optional[str]:
        logger.info(f"[LLM] Generating interview prep: jobTitle={job_title}, projects={len(project_experiences)}, workExp={len(work_experiences)}, missingSkills={len(missing_skills)}")
        
        no_data_label = self.messages.labels.get("no_data", "None")
        project_prefix = self.messages.labels.get("project_prefix", "Project: ")
        position_at = self.messages.labels.get("position_at", " at ")
        
        projects_text = "\n".join([
            f"- {project_prefix if p.get('project_name') else ''}{p.get('project_name', '')}: {p.get('description', '')}"
            for p in project_experiences
        ]) if project_experiences else no_data_label
        
        work_text = "\n".join([
            f"- {w.get('position', '')}{position_at}{w.get('company_name', '')}: {w.get('description', '')}"
            for w in work_experiences
        ]) if work_experiences else no_data_label
        
        missing_skills_text = ", ".join(missing_skills) if missing_skills else no_data_label
        
        system_message = self.prompts.interview_prep.get("system", "")
        user_template = self.prompts.interview_prep.get("user_template", "")
        
        prompt = user_template.format(
            job_title=job_title,
            job_requirements=job_requirements,
            projects_text=projects_text,
            work_text=work_text,
            missing_skills=missing_skills_text
        )
        
        prompt_length = len(prompt)
        logger.debug(f"[LLM] Interview prep prompt: length={prompt_length}, jobReqLength={len(job_requirements)}, projectsTextLength={len(projects_text)}, workTextLength={len(work_text)}")
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            logger.info(f"[LLM] Calling LLM API for interview prep: model={self.model}, stream=True")
            response = self._call_api(request_data)
            result = self._parse_response(response)
            logger.info(f"[LLM] Interview prep generated successfully: responseLength={len(result)}, preview={result[:100]}")
            return result
        except Exception as e:
            logger.error(f"[LLM] Failed to generate interview prep: {e}", exc_info=True)
            return None
    
    async def generate_interview_prep_stream(
        self,
        job_title: str,
        job_requirements: str,
        project_experiences: List[Dict[str, Any]],
        work_experiences: List[Dict[str, Any]],
        missing_skills: List[str]
    ) -> AsyncGenerator[str, None]:
        logger.info(f"[LLM] Generating interview prep stream: jobTitle={job_title}, projects={len(project_experiences)}, workExp={len(work_experiences)}, missingSkills={len(missing_skills)}")
        
        no_data_label = self.messages.labels.get("no_data", "None")
        project_prefix = self.messages.labels.get("project_prefix", "Project: ")
        position_at = self.messages.labels.get("position_at", " at ")
        
        projects_text = "\n".join([
            f"- {project_prefix if p.get('project_name') else ''}{p.get('project_name', '')}: {p.get('description', '')}"
            for p in project_experiences
        ]) if project_experiences else no_data_label
        
        work_text = "\n".join([
            f"- {w.get('position', '')}{position_at}{w.get('company_name', '')}: {w.get('description', '')}"
            for w in work_experiences
        ]) if work_experiences else no_data_label
        
        missing_skills_text = ", ".join(missing_skills) if missing_skills else no_data_label
        
        system_message = self.prompts.interview_prep.get("system", "")
        user_template = self.prompts.interview_prep.get("user_template", "")
        
        prompt = user_template.format(
            job_title=job_title,
            job_requirements=job_requirements,
            projects_text=projects_text,
            work_text=work_text,
            missing_skills=missing_skills_text
        )
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            async for chunk in self._stream_api_response(request_data):
                yield chunk
        except Exception as e:
            error_prefix = self.messages.stream.get("error_prefix", "Error: ")
            logger.error(f"[LLM] Failed to generate interview prep stream: {e}", exc_info=True)
            yield f"\n\n[{error_prefix}{str(e)}]"
    
    def parse_learning_plan(self, llm_output: str) -> List[Dict[str, Any]]:
        logger.debug(f"[LLM] Parsing learning plan: inputLength={len(llm_output) if llm_output else 0}")
        
        if not llm_output or not llm_output.strip():
            logger.warning(f"[LLM] Learning plan output is empty")
            return []
        
        skills = []
        sections = re.split(r'###\s+', llm_output)
        logger.debug(f"[LLM] Learning plan sections found: {len(sections) - 1}")
        
        for section in sections[1:]:
            lines = section.strip().split('\n')
            if not lines:
                continue
            
            skill_name = lines[0].strip()
            if not skill_name:
                continue
                
            skill_data = {
                "skill_name": skill_name,
                "reason": "",
                "learning_path": [],
                "resources": [],
                "estimated_weeks": 0
            }
            
            current_field = None
            for line in lines[1:]:
                line = line.strip()
                if not line:
                    continue
                
                if line.startswith("- 学习理由：") or line.startswith("学习理由："):
                    skill_data["reason"] = line.split("：", 1)[-1].strip()
                elif line.startswith("- 学习路径：") or line.startswith("学习路径："):
                    current_field = "learning_path"
                    path_text = line.split("：", 1)[-1].strip()
                    if path_text:
                        skill_data["learning_path"].append(path_text)
                elif line.startswith("- 推荐资源：") or line.startswith("推荐资源："):
                    current_field = "resources"
                    resource_text = line.split("：", 1)[-1].strip()
                    if resource_text:
                        skill_data["resources"].append(resource_text)
                elif line.startswith("- 预计周数：") or line.startswith("预计周数："):
                    weeks_text = line.split("：", 1)[-1].strip()
                    try:
                        match = re.search(r'\d+', weeks_text)
                        if match:
                            skill_data["estimated_weeks"] = int(match.group())
                    except (ValueError, AttributeError):
                        pass
                    current_field = None
                elif current_field == "learning_path":
                    if re.match(r'^\d+[\)）]', line) or line.startswith("-"):
                        skill_data["learning_path"].append(line.lstrip("- ").strip())
                elif current_field == "resources":
                    if line.startswith("-") or line.startswith("•"):
                        skill_data["resources"].append(line.lstrip("- •").strip())
                    else:
                        skill_data["resources"].append(line)
            
            if skill_name:
                skills.append(skill_data)
        
        logger.info(f"[LLM] Learning plan parsed: skillsCount={len(skills)}")
        return skills
    
    def _parse_interview_prep(self, llm_output: str) -> Dict[str, str]:
        logger.debug(f"[LLM] Parsing interview prep: inputLength={len(llm_output) if llm_output else 0}")
        
        result = {
            "project_tips": "",
            "possible_questions": "",
            "weakness_strategy": "",
            "strength_emphasis": ""
        }
        
        sections = re.split(r'##\s+\d+\.\s+', llm_output)
        logger.debug(f"[LLM] Interview prep sections found: {len(sections) - 1}")
        
        for section in sections[1:]:
            lines = section.strip().split('\n', 1)
            if not lines:
                continue
            
            title = lines[0].strip().lower()
            content = lines[1].strip() if len(lines) > 1 else ""
            
            if "项目讲解" in title or "项目" in title:
                result["project_tips"] = content
            elif "面试问题" in title or "问题" in title:
                result["possible_questions"] = content
            elif "弱项" in title or "应对" in title:
                result["weakness_strategy"] = content
            elif "优势" in title or "展示" in title:
                result["strength_emphasis"] = content
        
        logger.info(f"[LLM] Interview prep parsed: hasProjectTips={bool(result['project_tips'])}, hasQuestions={bool(result['possible_questions'])}, hasWeakness={bool(result['weakness_strategy'])}, hasStrength={bool(result['strength_emphasis'])}")
        return result
    
    def generate_candidate_evaluation(
        self,
        candidate_name: str,
        education: str,
        work_years: float,
        skills_text: str,
        projects_text: str,
        work_text: str,
        job_title: str,
        job_requirements: str,
        required_skills: str,
        missing_skills: str,
        match_score: int
    ) -> Optional[Dict[str, Any]]:
        """生成候选人综合评估报告"""
        logger.info(f"[LLM] Generating candidate evaluation: candidate={candidate_name}, jobTitle={job_title}, matchScore={match_score}")
        
        system_message = self.prompts.candidate_evaluation.get("system", "")
        user_template = self.prompts.candidate_evaluation.get("user_template", "")
        
        prompt = user_template.format(
            candidate_name=candidate_name,
            education=education,
            work_years=work_years,
            skills_text=skills_text,
            projects_text=projects_text,
            work_text=work_text,
            job_title=job_title,
            job_requirements=job_requirements,
            required_skills=required_skills,
            missing_skills=missing_skills,
            match_score=match_score
        )
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            logger.info(f"[LLM] Calling LLM API for candidate evaluation: model={self.model}, stream=False, responseFormat=json_object")
            response = self._call_api(request_data, stream=False)
            result = self._parse_response(response)
            logger.info(f"[LLM] Candidate evaluation received: responseLength={len(result)}")
            
            try:
                evaluation_data = json.loads(result)
                logger.info(f"[LLM] Candidate evaluation parsed successfully: hasStrengths={len(evaluation_data.get('strengths', []))}, hasWeaknesses={len(evaluation_data.get('weaknesses', []))}")
                return evaluation_data
            except json.JSONDecodeError as e:
                logger.error(f"[LLM] Failed to parse candidate evaluation JSON: {e}, rawResponse={result[:200]}")
                return None
        except Exception as e:
            logger.error(f"[LLM] Failed to generate candidate evaluation: {e}", exc_info=True)
            return None
    
    def generate_candidate_recommendation(
        self,
        candidate_name: str,
        education: str,
        work_years: float,
        key_skills: str,
        project_count: int,
        match_score: int,
        job_title: str,
        required_skills: str,
        missing_skills: str
    ) -> Optional[Dict[str, Any]]:
        """生成候选人推荐理由"""
        logger.info(f"[LLM] Generating candidate recommendation: candidate={candidate_name}, jobTitle={job_title}, matchScore={match_score}")
        
        system_message = self.prompts.candidate_recommendation.get("system", "")
        user_template = self.prompts.candidate_recommendation.get("user_template", "")
        
        prompt = user_template.format(
            candidate_name=candidate_name,
            education=education,
            work_years=work_years,
            key_skills=key_skills,
            project_count=project_count,
            match_score=match_score,
            job_title=job_title,
            required_skills=required_skills,
            missing_skills=missing_skills
        )
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            logger.info(f"[LLM] Calling LLM API for candidate recommendation: model={self.model}, stream=False, responseFormat=json_object")
            response = self._call_api(request_data, stream=False)
            result = self._parse_response(response)
            logger.info(f"[LLM] Candidate recommendation received: responseLength={len(result)}")
            
            try:
                recommendation_data = json.loads(result)
                logger.info(f"[LLM] Candidate recommendation parsed successfully: keyPoints={len(recommendation_data.get('key_points', []))}")
                return recommendation_data
            except json.JSONDecodeError as e:
                logger.error(f"[LLM] Failed to parse candidate recommendation JSON: {e}, rawResponse={result[:200]}")
                return None
        except Exception as e:
            logger.error(f"[LLM] Failed to generate candidate recommendation: {e}", exc_info=True)
            return None
    
    async def generate_hr_interview_questions_stream(
        self,
        candidate_name: str,
        education: str,
        work_years: float,
        skills_text: str,
        projects_text: str,
        work_text: str,
        missing_skills: str,
        job_title: str,
        job_requirements: str,
        required_skills: str
    ) -> AsyncGenerator[str, None]:
        """生成HR面试问题（流式输出）"""
        logger.info(f"[LLM] Generating HR interview questions stream: candidate={candidate_name}, jobTitle={job_title}")
        
        system_message = self.prompts.hr_interview_questions.get("system", "")
        user_template = self.prompts.hr_interview_questions.get("user_template", "")
        
        prompt = user_template.format(
            candidate_name=candidate_name,
            education=education,
            work_years=work_years,
            skills_text=skills_text,
            projects_text=projects_text,
            work_text=work_text,
            missing_skills=missing_skills,
            job_title=job_title,
            job_requirements=job_requirements,
            required_skills=required_skills
        )
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            async for chunk in self._stream_api_response(request_data):
                yield chunk
        except Exception as e:
            error_prefix = self.messages.stream.get("error_prefix", "Error: ")
            logger.error(f"[LLM] Failed to generate HR interview questions stream: {e}", exc_info=True)
            yield f"\n\n[{error_prefix}{str(e)}]"
    
    def generate_seeker_career_path(
        self,
        education: str,
        work_years: float,
        current_city: str,
        skills_text: str,
        work_experiences_text: str,
        project_experiences_text: str,
        target_position: str,
        target_city: str,
        target_skills: str
    ) -> Optional[Dict[str, Any]]:
        logger.info(f"[LLM] Generating seeker career path: targetPosition={target_position}, education={education}, workYears={work_years}")
        
        system_message = self.prompts.seeker_career_path.get("system", "")
        user_template = self.prompts.seeker_career_path.get("user_template", "")
        
        prompt = user_template.format(
            education=education,
            work_years=work_years,
            current_city=current_city,
            skills_text=skills_text,
            work_experiences_text=work_experiences_text,
            project_experiences_text=project_experiences_text,
            target_position=target_position,
            target_city=target_city,
            target_skills=target_skills
        )
        
        prompt_length = len(prompt)
        logger.debug(f"[LLM] Seeker career path prompt: length={prompt_length}")
        
        try:
            request_data = {
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_message},
                    {"role": "user", "content": prompt}
                ],
            }
            logger.info(f"[LLM] Calling LLM API for seeker career path: model={self.model}, stream=False, responseFormat=json_object")
            response = self._call_api(request_data, stream=False)
            result = self._parse_response(response)
            logger.info(f"[LLM] Seeker career path received: responseLength={len(result)}")
            
            try:
                career_path_data = json.loads(result)
                logger.info(f"[LLM] Seeker career path parsed successfully: hasProfileSummary={bool(career_path_data.get('profile_summary'))}, hasGapAnalysis={bool(career_path_data.get('gap_analysis'))}, hasCareerRoadmap={bool(career_path_data.get('career_roadmap'))}, hasLearningPlan={bool(career_path_data.get('learning_plan'))}, hasInterviewPrep={bool(career_path_data.get('interview_prep'))}")
                return career_path_data
            except json.JSONDecodeError as e:
                logger.error(f"[LLM] Failed to parse seeker career path JSON: {e}, rawResponse={result[:200]}")
                return None
                
        except Exception as e:
            logger.error(f"[LLM] Failed to generate seeker career path: {e}", exc_info=True)
            return None

