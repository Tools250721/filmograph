import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import AuthForm from "./components/AuthForm";
import { authAPI } from "../../utils/api";
import "./components/AuthForm.css";

const ResetPasswordPage = () => {
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  useEffect(() => {
    if (!token) {
      setError("유효하지 않은 링크입니다.");
    }
  }, [token]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (password !== confirmPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    if (password.length < 8) {
      setError("비밀번호는 8자 이상이어야 합니다.");
      return;
    }

    if (!token) {
      setError("유효하지 않은 토큰입니다.");
      return;
    }

    setLoading(true);

    try {
      await authAPI.resetConfirm(token, password);
      alert("비밀번호가 성공적으로 변경되었습니다.");
      navigate("/login");
    } catch (err) {
      setError(err.message || "비밀번호 변경에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthForm
      title="새 비밀번호"
      buttonText={loading ? "변경 중..." : "비밀번호 변경"}
      onSubmit={handleSubmit}
    >
      {error && <div style={{ color: "red", marginBottom: "10px" }}>{error}</div>}
      <input
        type="password"
        placeholder="새 비밀번호 (8자 이상)"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        disabled={loading || !token}
        minLength={8}
        required
      />
      <input
        type="password"
        placeholder="새 비밀번호 확인"
        value={confirmPassword}
        onChange={(e) => setConfirmPassword(e.target.value)}
        disabled={loading || !token}
        minLength={8}
        required
      />
    </AuthForm>
  );
};

export default ResetPasswordPage;
