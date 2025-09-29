import React from "react";

export default function LogoutButton() {
    function logout() {
        localStorage.removeItem("jwt");
        window.location.href = "/login";
    }

    return (
        <button
            type="button"
            onClick={logout}
            className="btn btn-primary" style={{ minWidth: "120px" }}
        >
            LOG OUT
        </button>
    );
}