import User from "../models/userModels.js";

export const getUser = async (req, res) => {
    const users = await User.findAll();
    res.json(users);
};

export const createUser async (req, res) => {
    const {name, email, passsword} = req.body;
    const newUser = await User.create({name, email, passsword});
    res.status(201).json(newUser);
};