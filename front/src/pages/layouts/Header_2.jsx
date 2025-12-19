import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import Logo from "../../assets/icons/Logo_2.svg";
import { ReactComponent as SearchIcon } from "../../assets/icons/SearchIcon.svg";
import { ReactComponent as UserIcon } from "../../assets/icons/UserIcon.svg";
import AuthModal from "../../components/AuthModal";
import { getToken } from "../../utils/api";
import { useUser } from "../../contexts/UserContext";
import "./Header_2.css";

const Header_2 = ({ onLoginSuccess }) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const navigate = useNavigate();
  const { userProfile } = useUser();

  const handleSearch = (event) => {
    event.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/search?q=${searchTerm}`);
    }
  };

  const handleProfileClick = (e) => {
    e.preventDefault();
    if (getToken()) {
      // 로그인되어 있으면 MyPage로 이동
      navigate("/my");
    } else {
      // 로그인되어 있지 않으면 모달 표시
      setIsAuthModalOpen(true);
    }
  };

  const handleLoginSuccess = () => {
    setIsAuthModalOpen(false);
    if (onLoginSuccess) {
      onLoginSuccess();
    }
    // 로그인 성공 후 MyPage로 이동
    navigate("/my");
  };

  return (
    <>
      <header className="header-two">
        <Link to="/" className="header-two__logo">
          <img src={Logo} alt="로고" />
        </Link>

        {/* 메뉴 */}
        <div className="header-two__menu">
          <form onSubmit={handleSearch} className="header-two__search-form">
            <div className="header-two__search-field">
              <SearchIcon className="header-two__search-icon" />
              <input
                type="text"
                placeholder="콘텐츠, 인물, 연도 검색"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="header-two__search-input"
              />
            </div>
          </form>

          <a
            href="#"
            onClick={handleProfileClick}
            className="header-two__profile-link"
            style={{ display: "flex", alignItems: "center", justifyContent: "center" }}
          >
            {userProfile?.profileImage ? (
              <img
                src={
                  userProfile.profileImage.startsWith("http")
                    ? userProfile.profileImage
                    : `${process.env.REACT_APP_API_URL || "http://localhost:8080"}${userProfile.profileImage}`
                }
                alt="프로필"
                style={{
                  width: "24px",
                  height: "24px",
                  borderRadius: "50%",
                  objectFit: "cover",
                }}
                onError={(e) => {
                  e.target.style.display = "none";
                }}
              />
            ) : (
              <UserIcon />
            )}
          </a>
        </div>
      </header>

      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onLoginSuccess={handleLoginSuccess}
        initialMode="login"
      />
    </>
  );
};

export default Header_2;
