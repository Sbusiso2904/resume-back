import express from "express";
import cors from "cors";
import userRoutes from "./routes/userRoutes.js";
import errorHandler from "./middleware/errorMiddleware.js";


const app = express();

app.use(cors());
app.use(express.json()); //parse JSON
app.use("/api/users", userRoutes);

app.use(errorHandler);

export default app;