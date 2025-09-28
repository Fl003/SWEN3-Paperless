import { Navigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

function isTokenValid(token) {
    /* try {
        const { exp } = jwtDecode(token);
        window.alert(exp + " " + exp * 1000 > Date.now());
        return exp * 1000 > Date.now();
    } catch {
        return false;
    } */

    return true;
}

export default function ProtectedRoute({ children }) {
    const token = localStorage.getItem("jwt");

    if (!token || !isTokenValid(token)) {
        // Token fehlt oder abgelaufen → Weiterleitung
        return <Navigate to="/login" replace />;
    }

    return children;
}