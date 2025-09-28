import React from 'react'
import { useNavigate } from "react-router-dom";
import { login } from "../services/api.js";

export default function Login() {
    const [username, setUsername] = React.useState('')
    const [password, setPassword] = React.useState('')

    const navigate = useNavigate();

    async function submit(e) {
        e.preventDefault();

        await login(username, password)
            .then(data => {
                console.log("Token:", data.token);
                localStorage.setItem("jwt", data.token);
                navigate("/");
            })
            .catch(err => {
                console.error("Error during login:", err);
            });
    }

    return (
        <div className="login" onSubmit={submit}>
            <div className="bg">
                <span>PAPER</span>
                <span>LESS</span>
            </div>
            <form>
                <div className="input-wrapper">
                    <input
                        type="text"
                        id="username"
                        name="username"
                        placeholder="John Doe"
                        onChange={e => {
                            setUsername(e.target.value);
                        }}
                        required/>
                    <label htmlFor="username">Username</label>
                </div>
                <div className="input-wrapper">
                    <input
                        type="password"
                        id="password"
                        name="password"
                        placeholder="*********"
                        onChange={e => {
                            setPassword(e.target.value);
                        }}
                        required/>
                    <label htmlFor="password">Password</label>
                </div>
                <button type="submit" className="btn btn-primary">Login</button>
            </form>
        </div>
    )
}