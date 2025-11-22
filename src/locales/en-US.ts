export default {
  tabBar: {
    jobs: 'Jobs',
    chat: 'Messages',
    profile: 'Profile'
  },
  navigation: {
    jobs: 'Jobs',
    chat: 'Messages',
    profile: 'Profile'
  },
  pages: {
    jobs: {
      title: 'Jobs',
      matchScore: 'Match',
      months: 'mo',
      loading: 'Loading...',
      loadError: 'Failed to load',
      noJobs: 'No job recommendations',
    },
    chat: {
      title: 'Messages',
      
    },
    profile: {
      title: 'Profile',
      username: 'Username',
      email: 'Email',
      phone: 'Phone',
      userType: 'User Type',
      avatar: 'Avatar',
      loading: 'Loading...',
      loadError: 'Failed to load',
    }
  },
  degree: {
    highSchoolOrBelow: 'High School',
    associateDegree: 'Associate',
    bachelorDegree: 'Bachelor',
    masterDegree: 'Master',
    doctorDegree: 'Doctor'
  },
  auth: {
    login: {
      title: 'Login',
      accountLogin: 'Account Login',
      subtitle: 'Login to discover more',
      username: 'Username',
      password: 'Password',
      loginButton: 'Login',
      loggingIn: 'Logging in...',
      loginSuccess: 'Login successful',
      loginIssues: 'Login issues?',
      noAccount: "Don't have an account?",
      goToRegister: 'Register',
      validation: {
        usernameRequired: 'Username is required',
        passwordRequired: 'Password is required'
      }
    },
    register: {
      title: 'Register',
      selectIdentity: 'What is your role?',
      jobSeeker: 'Job Seeker',
      hr: 'HR',
      username: 'Username',
      password: 'Password',
      email: 'Email',
      phone: 'Phone',
      gender: 'Gender',
      verifyCode: 'Verification Code',
      sendCode: 'Send Code',
      resendCode: 'Resend',
      registerButton: 'Register',
      backToPrevious: '← Back',
      registerIssues: 'Registration issues?',
      male: 'Male',
      female: 'Female',
      preferNotToSay: 'Prefer not to say',
      validation: {
        usernameRequired: 'Username is required',
        passwordRequired: 'Password is required',
        passwordMinLength: 'Password must be at least 6 characters',
        emailRequired: 'Email is required',
        emailInvalid: 'Invalid email format',
        phoneRequired: 'Phone number is required',
        phoneInvalid: 'Phone number must be 11 digits',
        genderRequired: 'Please select gender',
        verifyCodeRequired: 'Verification code is required'
      },
      sending: 'Sending...',
      codeSent: 'Code sent',
      registering: 'Registering...',
      registerSuccess: 'Registration successful'
    },
    help: {
      title: 'Help',
      contactMessage: 'Please contact SmartHire development team: sincetodayz@gmail.com'
    }
  }
};
