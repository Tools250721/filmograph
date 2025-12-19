import React from "react";
import Logo from "../../../assets/icons/Logo_2.svg";
import "./AuthForm.css";

const AuthForm = ({ title, buttonText, children, onSubmit, links }) => {
  return (
    <div className="auth-container">
      <form className="auth-form" onSubmit={onSubmit}>
        <img src={Logo} className="auth-logo" />
        <h2 className="auth-title">{title}</h2>

        <div className="auth-inputs">{children}</div>

        <button type="submit" className="auth-button">
          {buttonText}
        </button>

        {links}
      </form>
    </div>
  );
};

export default AuthForm;
