import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthForm from "./components/AuthForm";
import { authAPI } from "../../utils/api";
import "./components/AuthForm.css";

const SignupPage = () => {
  const [name, setName] = useState("");
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
      await authAPI.register(name, email, password);
      // 회원가입 성공 시 로그인 페이지로 이동
      navigate("/login");
    } catch (err) {
      setError(err.message || "회원가입에 실패했습니다. 입력 정보를 확인해주세요.");
    } finally {
      setLoading(false);
    }
  };

  const loginLinks = (
    <div className="auth-links">
      <div>
        <span className="auth-link-text">이미 계정이 있으신가요? </span>
        <Link to="/login" className="auth-link-green">
          로그인
        </Link>
      </div>
    </div>
  );

  return (
    <AuthForm
      title="회원가입"
      buttonText={loading ? "가입 중..." : "회원가입"}
      onSubmit={handleSubmit}
      links={loginLinks}
    >
      {error && <div style={{ color: "red", marginBottom: "10px" }}>{error}</div>}
      <input
        type="text"
        placeholder="이름"
        value={name}
        onChange={(e) => setName(e.target.value)}
        disabled={loading}
        required
      />
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
        placeholder="비밀번호 (8자 이상)"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        disabled={loading}
        minLength={8}
        required
      />
    </AuthForm>
  );
};

export default SignupPage;
