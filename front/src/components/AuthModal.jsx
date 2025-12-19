import React, { useState } from "react";
import Logo from "../assets/icons/Logo_2.svg";
import { authAPI, getToken } from "../utils/api";
import Modal from "../pages/common/Modal";
import "./AuthModal.css";

const AuthModal = ({
  isOpen,
  onClose,
  onLoginSuccess,
  initialMode = "login",
}) => {
  const [mode, setMode] = useState(initialMode); // "login" or "signup"
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await authAPI.login(email, password);
      setEmail("");
      setPassword("");
      if (onLoginSuccess) {
        onLoginSuccess();
      }
      onClose();
    } catch (err) {
      setError(
        err.message ||
          "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSignup = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await authAPI.register(name, email, password);
      // 회원가입 성공 시 자동으로 로그인
      await authAPI.login(email, password);
      setName("");
      setEmail("");
      setPassword("");
      if (onLoginSuccess) {
        onLoginSuccess();
      }
      onClose();
    } catch (err) {
      setError(
        err.message || "회원가입에 실패했습니다. 입력 정보를 확인해주세요."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleModeSwitch = () => {
    setMode(mode === "login" ? "signup" : "login");
    setError("");
    setEmail("");
    setPassword("");
    setName("");
  };

  const handleClose = () => {
    setError("");
    setEmail("");
    setPassword("");
    setName("");
    setMode(initialMode);
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose}>
      <div className="auth-modal">
        <form
          className="auth-modal__form"
          onSubmit={mode === "login" ? handleLogin : handleSignup}
        >
          <img src={Logo} className="auth-modal__logo" alt="로고" />
          <h2 className="auth-modal__title">
            {mode === "login" ? "로그인" : "회원가입"}
          </h2>

          <div className="auth-modal__inputs">
            {error && <div className="auth-modal__error">{error}</div>}
            {mode === "signup" && (
              <input
                type="text"
                placeholder="이름"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={loading}
                required
              />
            )}
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
              placeholder={
                mode === "login" ? "비밀번호" : "비밀번호 (8자 이상)"
              }
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              minLength={mode === "signup" ? 8 : undefined}
              required
            />
          </div>

          <button
            type="submit"
            className="auth-modal__button"
            disabled={loading}
          >
            {loading
              ? mode === "login"
                ? "로그인 중..."
                : "가입 중..."
              : mode === "login"
              ? "로그인"
              : "회원가입"}
          </button>

          <div className="auth-modal__links">
            {mode === "login" ? (
              <>
                <a
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    // 비밀번호 찾기 페이지로 이동 (필요시 구현)
                  }}
                  className="auth-modal__link"
                >
                  비밀번호를 잊어버리셨나요?
                </a>
                <div>
                  <span className="auth-modal__link-text">
                    계정이 없으신가요?{" "}
                  </span>
                  <button
                    type="button"
                    onClick={handleModeSwitch}
                    className="auth-modal__link-button"
                  >
                    회원가입
                  </button>
                </div>
              </>
            ) : (
              <div>
                <span className="auth-modal__link-text">
                  이미 계정이 있으신가요?{" "}
                </span>
                <button
                  type="button"
                  onClick={handleModeSwitch}
                  className="auth-modal__link-button"
                >
                  로그인
                </button>
              </div>
            )}
          </div>
        </form>
      </div>
    </Modal>
  );
};

export default AuthModal;
