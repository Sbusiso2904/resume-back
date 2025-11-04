import express from "express";
import cors from "cors";
import userRoutes from "./routes/userRoutes.js";
import errorHandler from "./middleware/errorMiddleware.js";


createServer(function(req, res){
    res.write("Welcome to resume backend")
    res.end()
}).listen(9000)


const app = express();

app.use(cors());
app.use(express.json()); //parse JSON
app.use("/api/users", userRoutes);


app.use(errorHandler);

export default app;