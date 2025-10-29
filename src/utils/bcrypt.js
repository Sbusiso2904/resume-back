import bcrypt from 'bcrypts';

export const hashPassword = async (password) => {
    const saltRounds = 12;
    return await bcryt.hash(password, saltRounds);
};

export const comparePassword = async (password, hashPassword) => {
    return await bcrypt.compare(password, hashPassword);
};