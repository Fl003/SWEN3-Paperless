import React from 'react'
import { useNavigate } from "react-router-dom";
import { login } from "../services/api.js";

export default function Login() {
    const [username, setUsername] = React.useState('')
    const [password, setPassword] = React.useState('')
    const [errorMessage, setErrorMessage] = React.useState('')

    const navigate = useNavigate();

    async function submit(e) {
        e.preventDefault();
        setErrorMessage('');

        try {
            const data = await login(username, password);
            if (data?.token) {
                localStorage.setItem('jwt', data.token);
                navigate('/');
            } else {
                setErrorMessage('Wrong username or password');
            }
        } catch (err) {
            // 401 from backend will land here
            setErrorMessage('Wrong username or password');
            console.error(err);
        }
    }

    return (
        <div className="login">
            <div className="bg">
                <span>PAPER</span>
                <span>LESS</span>
            </div>
            <form onSubmit={submit}>
                {errorMessage && <div className="alert">{errorMessage}</div>}

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
                        placeholder="*******"
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