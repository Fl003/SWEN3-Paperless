import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import Documents from './pages/Documents.jsx'
import DocumentDetails from './pages/DocumentDetails.jsx'

export default function App() {
    return (
        <>
            <header className="app-header">
                <h1>PAPERLESS</h1>
                <nav>
                    <Link to="/">Dashboard</Link>
                </nav>
            </header>
            <main className="container">
                <Routes>
                    <Route path="/" element={<Documents />} />
                    <Route path="/document/:id" element={<DocumentDetails />} />
                </Routes>
            </main>
        </>
    )
}
