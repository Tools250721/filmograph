import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthForm from "./components/AuthForm";
import { authAPI } from "../../utils/api";
import "./components/AuthForm.css";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await authAPI.login(email, password);
      // 로그인 성공 시 메인 페이지로 이동
      navigate("/");
    } catch (err) {
      setError(err.message || "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.");
    } finally {
      setLoading(false);
    }
  };

  const loginLinks = (
    <div className="auth-links">
      <Link to="/find-password">비밀번호를 잊어버리셨나요?</Link>
      <div>
        <span className="auth-link-text">계정이 없으신가요? </span>
        <Link to="/signup" className="auth-link-green">
          회원가입
        </Link>
      </div>
    </div>
  );

  return (
    <AuthForm
      title="로그인"
      buttonText={loading ? "로그인 중..." : "로그인"}
      onSubmit={handleSubmit}
      links={loginLinks}
    >
      {error && <div style={{ color: "red", marginBottom: "10px" }}>{error}</div>}
      <input
        type="email"
        placeholder="이메일"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        disabled={loading}
        required
      />
      <input
        type="password"
        placeholder="비밀번호"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        disabled={loading}
        required
      />
    </AuthForm>
  );
};

export default LoginPage;
