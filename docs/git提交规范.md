# SmartHire 项目 git 提交规范

项目采用约定式提交（Conventional Commits） 标准，格式和具体说明如下：
```
[scope]: <description>

[body]

[footer(s)]
```

- 标题（必填）
- 格式：<type>(<scope>): <description>
- 示例：feat(login): 新增用户登录功能
- 字段说明：
    - 类型（type）：使用明确动词：
        - build：修改项目构建系统，如修改依赖库、外部接口或升级版本
        - feat：添加新功能
        - fix：修复问题
        - docs：文档变更、例如修改 README 文件、API 文档
        - style：代码样式调整（不影响功能）
        - refactor：代码重构
        - test：添加、修改、删除测试用例
        - pref：优化性能
        - chore：构建工具或辅助脚本变更
    - 范围（scope）：可选，用括号包裹，如模块名 user 或文件名 api.js。
    - 描述（description）：简明扼要（≤50字符），以动词开头，如 修复登录超时问题。
- 正文（body，可选）
    - 内容：详细说明变更背景、原因和结果，每行≤72字符。
    - 示例：
```
- 添加登录页面和表单验证逻辑
- 实现与后端的认证接口对接
- 优化错误提示反馈
```
- 脚注（footer，可选）
    - 关联问题：使用 Closes #123 或 Fixes #456 关联缺陷或需求。
    - 破坏性变更：若引入不兼容改动，需以 BREAKING CHANGE: 开头说明。