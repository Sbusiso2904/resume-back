import express from 'express';
import { authenticateToken } from '../middleware/auth.js';
import { getProfile } from '../controllers/authController.js';

const router = express.Router();

router.get('/profile', authenticateToken, getProfile);

export default router;