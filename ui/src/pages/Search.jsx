import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { searchDocuments } from "../services/api";

export default function Search() {
    const navigate = useNavigate();
    const [params, setParams] = useSearchParams();

    const initialQ = params.get("q") ?? "";
    const [q, setQ] = useState(initialQ);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [results, setResults] = useState([]);

    const hasQuery = useMemo(() => (q ?? "").trim().length > 0, [q]);

    async function runSearch(queryText) {
        const trimmed = (queryText ?? "").trim();
        if (!trimmed) {
            setResults([]);
            setError("");
            return;
        }

        setLoading(true);
        setError("");

        try {
            const data = await searchDocuments(trimmed, 0, 20);
            setResults(Array.isArray(data) ? data : (data?.items ?? []));
        } catch (e) {
            setResults([]);
            setError(e?.message ?? "Search failed");
        } finally {
            setLoading(false);
        }
    }

    function onSubmit(e) {
        e.preventDefault();
        const trimmed = (q ?? "").trim();
        setParams(trimmed ? { q: trimmed } : {});
        runSearch(trimmed);
    }

    // If someone opens /search?q=something directly
    useEffect(() => {
        if (initialQ && initialQ !== q) setQ(initialQ);
        if (initialQ) runSearch(initialQ);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <div className="searchPage">
            <div className="searchHeader">
                <div className="paperlessLogo" onClick={() => navigate("/")}>
                    PAPERLESS
                </div>

                <form className="searchBar" onSubmit={onSubmit}>
                    <input
                        value={q}
                        onChange={(e) => setQ(e.target.value)}
                        placeholder="Search your documents…"
                        aria-label="Search"
                    />
                    <button type="submit" disabled={!hasQuery || loading}>
                        Search
                    </button>
                </form>
            </div>

            <div className="searchBody">
                {loading && <div className="searchInfo">Searching…</div>}
                {error && <div className="searchError">{error}</div>}

                {!loading && !error && results.length === 0 && hasQuery && (
                    <div className="searchInfo">No results.</div>
                )}

                <div className="searchResults">
                    {results.map((r) => (
                        <div className="searchResult" key={r.documentId ?? r.id ?? Math.random()}>
                            <div className="searchResultTop">
                                <Link
                                    className="searchResultTitle"
                                    to={`/document/${encodeURIComponent(r.documentId)}`}
                                >
                                    {r.name ?? "(untitled)"}
                                </Link>

                                {typeof r.score === "number" && (
                                    <span className="searchResultScore">
                    score: {r.score.toFixed(2)}
                  </span>
                                )}
                            </div>

                            {r.snippet && <div className="searchResultSnippet">{r.snippet}</div>}

                            <div className="searchResultMeta">
                                {r.status && <span className="pill">{r.status}</span>}
                                {(r.tags || []).slice(0, 8).map((t) => (
                                    <span className="pill" key={t}>
                    {t}
                  </span>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}