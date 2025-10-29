import jwt from 'jsonwebtoken';


export const generatetoken = (userId) => {
    return jwt.sign({UserId}, JWT_SECRET, {
        exiresin: JWT_EXPIRES_IN,
    });
};

export const verifyToken = Token => {
    return jwt.verify(token, JWT_SECRET);
};

