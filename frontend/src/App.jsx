import { useState } from "react";
import { analyzeCode, optimizeCode } from "./api/api";
import "./App.css";
function App() {
    const [code, setCode] = useState("");
    const [analyzeResult, setAnalyzeResult] = useState(null);
    const [optimizeResult, setOptimizeResult] = useState(null);
    const [loadingAnalyze, setLoadingAnalyze] = useState(false);
    const [loadingOptimize, setLoadingOptimize] = useState(false);
    const [responseTime, setResponseTime] = useState(null);
    const [context, setContext] = useState("");

    const handleAnalyze = async () => {
        setAnalyzeResult(null);
        setOptimizeResult(null);
        setResponseTime(null);
        setLoadingAnalyze(true);

        const start = performance.now();

        try {
            const data = await analyzeCode({ code, context });
            setAnalyzeResult(data);

            const end = performance.now();
            setResponseTime(Math.round(end - start));
        } catch (err) {
            console.error(err);
            alert("Analyze failed");
        }

        setLoadingAnalyze(false);
    };

    const handleOptimize = async () => {
        if (!analyzeResult) return;

        if (
            !analyzeResult.category ||
            analyzeResult.category === "CLEAN" ||
            analyzeResult.category === "SYNTAX_INVALID"
        ) {
            return;
        }

        setOptimizeResult(null);  // 🔥 reset previous result
        setLoadingOptimize(true);

        try {
            const result = await optimizeCode({
                code,
                context,
                category: analyzeResult.category
            });

            setOptimizeResult(result);
        } catch (err) {
            console.error(err);
            alert("Optimize failed");
        }

        setLoadingOptimize(false);
    };

    return (
        <>
            {(loadingAnalyze || loadingOptimize) && (
                <div className="overlay">
                    <div className="thinking-card">
                        <div className="loader">
                            <div></div>
                            <div></div>
                            <div></div>
                        </div>
                        <p>
                            {loadingAnalyze ? "Analyzing code..." : "Optimizing code..."}
                        </p>
                    </div>
                </div>
            )}

            <div className="container">
            <h1>AI Java Review Assistant</h1>

                <div className="editor-card">
      <textarea
          placeholder="Paste your Java code here..."
          value={code}
          onChange={(e) => setCode(e.target.value)}
      />
                    <textarea
                        placeholder="Optional: Add context about what this code is supposed to do..."
                        value={context}
                        onChange={(e) => setContext(e.target.value)}
                        className="context-area"
                    />
                    <button onClick={handleAnalyze} disabled={loadingAnalyze}>
                        Analyze Code
                    </button>
                </div>

                {analyzeResult && (
                    <div className="section">
                        <h2>Analysis</h2>
                        {responseTime && (
                            <div className="response-time">
                                ⏱ {responseTime} ms
                            </div>
                        )}

                        <p><strong>Summary:</strong> {analyzeResult.summary}</p>
                        <p>
                            <strong>Category:</strong>{" "}
                            <span className={`category ${analyzeResult.category.toLowerCase()}`}>
        {analyzeResult.category}
    </span>
                        </p>
                        <p><strong>Time Complexity:</strong> {analyzeResult.timeComplexity}</p>
                        <p><strong>Space Complexity:</strong> {analyzeResult.spaceComplexity}</p>

                        <h3>Issues</h3>
                        {analyzeResult.issues.length === 0 ? (
                            <p>No issues detected.</p>
                        ) : (
                            <ul>
                                {analyzeResult.issues.map((issue, index) => (
                                    <li key={index}>{issue}</li>
                                ))}
                            </ul>
                        )}

                        {analyzeResult.category !== "CLEAN" &&
                            analyzeResult.category !== "SYNTAX_INVALID" && (
                                <button
                                    onClick={handleOptimize}
                                    disabled={loadingOptimize}
                                >
                                    Optimize Code
                                </button>
                            )}
                    </div>
                )}

                {optimizeResult?.optimizedCode && (
                    <div className="section">
                        <h2>Optimized Code</h2>

                        <div className="section-header">
                            <button
                                className="copy-btn"
                                onClick={() =>
                                    navigator.clipboard.writeText(
                                        optimizeResult.optimizedCode
                                    )
                                }
                            >
                                Copy
                            </button>
                        </div>

                        <pre className="code-block">
            {optimizeResult.optimizedCode}
        </pre>

                        <p>
                            <strong>Explanation:</strong>{" "}
                            {optimizeResult.optimizationExplanation}
                        </p>
                    </div>
                )}
        </div>
            </>
    );
}

export default App;