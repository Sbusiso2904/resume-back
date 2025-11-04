import { authService } from '../services/authService.js';
import { authValidator } from '../utils/validators.js';

export const register = async (req, res) => {
  try {
    const { email, username, password } = req.body;

    // Validate input
    const validation = authValidator.validateRegistration({ email, username, password });
    if (!validation.isValid) {
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: validation.results,
      });
    }

    // Register user
    const { user, token } = await authService.registerUser(email, username, password);

    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      data: {
        user,
        token,
      },
    });
  } catch (error) {
    console.error('Registration error:', error);

    if (error.message.includes('already exists')) {
      return res.status(400).json({
        success: false,
        message: error.message,
      });
    }

    res.status(500).json({
      success: false,
      message: 'Internal server error',
    });
  }
};

export const login = async (req, res) => {
  try {
    const { email, password } = req.body;

    // Validate email
    const emailValidation = authValidator.validateEmail(email);
    if (!emailValidation.isValid) {
      return res.status(400).json({
        success: false,
        message: 'Invalid email format',
      });
    }

    // Login user
    const { user, token } = await authService.loginUser(email, password);

    res.json({
      success: true,
      message: 'Login successful',
      data: {
        user,
        token,
      },
    });
  } catch (error) {
    console.error('Login error:', error);

    if (error.message.includes('Invalid credentials')) {
      return res.status(401).json({
        success: false,
        message: error.message,
      });
    }

    res.status(500).json({
      success: false,
      message: 'Internal server error',
    });
  }
};

export const getProfile = async (req, res) => {
  try {
    const user = await authService.getUserById(req.userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found',
      });
    }

    res.json({
      success: true,
      data: {
        user,
      },
    });
  } catch (error) {
    console.error('Get profile error:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error',
    });
  }
};