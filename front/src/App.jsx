import { Routes, Route } from "react-router-dom";
import DetailPage from "./pages/detailPage/DetailPage";
import CommentsPage from "./pages/commentsPage/CommentsPage";
import SearchPage from "./pages/searchPage/SearchPage";
import MyCommentPage from "./pages/myCommentsPage/MyCommentsPage";
import MainPage from "./pages/mainPage/MainPage";
import MyPage from "./pages/myPage/MyPage";
import RatedPage from "./pages/myPage/RatedPage";
import CommentedPage from "./pages/myPage/CommentedPage";

import LoginPage from "./pages/LoginPage/LoginPage";
import SignupPage from "./pages/LoginPage/SignupPage";
import FindPasswordPage from "./pages/LoginPage/FindPasswordPage";
import ResetPasswordPage from "./pages/LoginPage/ResetPasswordPage";

import WatchlistPage from "./pages/myPage/WatchlistPage";
import WatchingPage from "./pages/myPage/WatchingPage";

import { UserProvider } from "./contexts/UserContext";

import "./index.css";

export default function App() {
  return (
    <UserProvider>
      <div
        style={{
          fontFamily:
            'Pretendard, system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans KR", "Apple SD Gothic Neo", "Malgun Gothic", sans-serif',
          WebkitFontSmoothing: "antialiased",
          MozOsxFontSmoothing: "grayscale",
          backgroundColor: "#f8fafc",
          minHeight: "100vh",
        }}
      >
        <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/movie/:movieId" element={<DetailPage />} />

        <Route path="/comments" element={<CommentsPage />} />

        <Route path="/commented" element={<CommentedPage />} />

        <Route path="/search" element={<SearchPage />} />
        <Route path="/mycomments" element={<MyCommentPage />} />
        <Route path="/rated" element={<RatedPage />} />
        <Route path="/my" element={<MyPage />} />

        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/find-password" element={<FindPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        <Route path="/watchlist" element={<WatchlistPage />} />
        <Route path="/watching" element={<WatchingPage />} />
        </Routes>
      </div>
    </UserProvider>
  );
}
