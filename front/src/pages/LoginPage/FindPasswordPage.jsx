import React, { useState } from "react";
import AuthForm from "./components/AuthForm";
import { authAPI } from "../../utils/api";
import "./components/AuthForm.css";

const FindPasswordPage = () => {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      await authAPI.resetRequest(email);
      setSuccess(`${email}으로 비밀번호 재설정 링크를 보냈습니다.`);
    } catch (err) {
      setError(err.message || "요청에 실패했습니다. 이메일을 확인해주세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthForm
      title="비밀번호 재설정"
      buttonText={loading ? "요청 중..." : "인증 메일 요청"}
      onSubmit={handleSubmit}
    >
      {error && <div style={{ color: "red", marginBottom: "10px" }}>{error}</div>}
      {success && <div style={{ color: "green", marginBottom: "10px" }}>{success}</div>}
      <input
        type="email"
        placeholder="이메일"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        disabled={loading}
        required
      />
    </AuthForm>
  );
};

export default FindPasswordPage;
