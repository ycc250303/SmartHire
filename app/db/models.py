from sqlalchemy import Column, Integer, String, Text, DateTime, Date, ForeignKey, DECIMAL
from sqlalchemy.dialects.mysql import TINYINT
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from datetime import datetime

Base = declarative_base()


class User(Base):
    __tablename__ = "user"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    username = Column(String(50), nullable=False, unique=True)
    password = Column(String(255), nullable=False)
    email = Column(String(100))
    phone = Column(String(20))
    gender = Column(Integer)
    user_type = Column(Integer, nullable=False)
    status = Column(Integer, default=1)
    avatar_url = Column(String(255))
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    last_login_at = Column(DateTime)


class JobSeeker(Base):
    __tablename__ = "job_seeker"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("user.id"), nullable=False, unique=True)
    real_name = Column(String(50), nullable=False)
    birth_date = Column(DateTime)
    current_city = Column(String(50))
    education = Column(TINYINT)
    job_status = Column(TINYINT)
    graduation_year = Column(String(10))
    work_experience_year = Column(Integer, default=0)
    internship_experience = Column(TINYINT, default=0)
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)


class JobSeekerExpectation(Base):
    __tablename__ = "job_seeker_expectation"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    job_seeker_id = Column(Integer, ForeignKey("job_seeker.id"), nullable=False)
    expected_position = Column(String(100))
    work_city = Column(String(50))
    salary_min = Column(DECIMAL(10, 2))
    salary_max = Column(DECIMAL(10, 2))
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)


class JobInfo(Base):
    __tablename__ = "job_info"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    company_id = Column(Integer, ForeignKey("company.id"), nullable=False)
    hr_id = Column(Integer, ForeignKey("hr_info.id"), nullable=False)
    job_title = Column(String(100), nullable=False)
    job_category = Column(String(50))
    department = Column(String(50))
    city = Column(String(50), nullable=False)
    address = Column(String(255))
    salary_min = Column(DECIMAL(10, 2))
    salary_max = Column(DECIMAL(10, 2))
    salary_months = Column(Integer, default=12)
    education_required = Column(TINYINT)
    job_type = Column(TINYINT)
    experience_required = Column(TINYINT)
    internship_days_per_week = Column(Integer)
    internship_duration_months = Column(Integer)
    description = Column(Text, nullable=False)
    responsibilities = Column(Text)
    requirements = Column(Text)
    status = Column(TINYINT, default=1)
    audit_status = Column(String(20))
    company_audit_status = Column(String(20))
    view_count = Column(Integer, default=0)
    application_count = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    published_at = Column(DateTime)
    submitted_at = Column(DateTime)
    audited_at = Column(DateTime)


class JobSkillRequirement(Base):
    __tablename__ = "job_skill_requirement"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    job_id = Column(Integer, ForeignKey("job_info.id"), nullable=False)
    skill_name = Column(String(50), nullable=False)
    is_required = Column(TINYINT, default=1)
    created_at = Column(DateTime, default=datetime.now)


class EducationExperience(Base):
    __tablename__ = "education_experience"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    job_seeker_id = Column(Integer, ForeignKey("job_seeker.id"), nullable=False)
    school_name = Column(String(100), nullable=False)
    major = Column(String(100))
    education = Column(TINYINT, nullable=False)
    start_year = Column(Date, nullable=False)
    end_year = Column(Date)
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)


class Skill(Base):
    __tablename__ = "skill"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    job_seeker_id = Column(Integer, ForeignKey("job_seeker.id"), nullable=False)
    skill_name = Column(String(50), nullable=False)
    level = Column(TINYINT)
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)


class WorkExperience(Base):
    __tablename__ = "work_experience"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    job_seeker_id = Column(Integer, ForeignKey("job_seeker.id"), nullable=False)
    company_name = Column(String(100), nullable=False)
    position = Column(String(100), nullable=False)
    department = Column(String(50))
    start_date = Column(DateTime, nullable=False)
    end_date = Column(DateTime)
    is_internship = Column(TINYINT, default=0)
    description = Column(Text)
    achievements = Column(Text)
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)


class ProjectExperience(Base):
    __tablename__ = "project_experience"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    job_seeker_id = Column(Integer, ForeignKey("job_seeker.id"), nullable=False)
    project_name = Column(String(100), nullable=False)
    project_role = Column(String(50))
    start_date = Column(DateTime, nullable=False)
    end_date = Column(DateTime)
    description = Column(Text)
    responsibility = Column(Text)
    achievement = Column(Text)
    project_url = Column(String(255))
    created_at = Column(DateTime, default=datetime.now)
    updated_at = Column(DateTime, default=datetime.now, onupdate=datetime.now)


class Company(Base):
    __tablename__ = "company"
    
    id = Column(Integer, primary_key=True, autoincrement=True)


class HrInfo(Base):
    __tablename__ = "hr_info"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("user.id"), nullable=False, unique=True)

