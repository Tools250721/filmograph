import { useState } from "react";
import UpIcon from "../../../assets/icons/Dropdown_up.svg";
import DownIcon from "../../../assets/icons/Dropdown_down.svg";
import "./SortDropdown.css";

export default function SortDropdown({ sortOption, setSortOption }) {
  const [open, setOpen] = useState(false);

  const options = [
    { value: "like", label: "좋아요 순" },
    { value: "highRating", label: "높은 평가 순" },
    { value: "lowRating", label: "낮은 평가 순" },
    { value: "date", label: "작성 순" },
  ];

  return (
    <div className={`sort-dropdown ${open ? "is-open" : ""}`}>
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        className="sort-dropdown__trigger"
        aria-haspopup="listbox"
        aria-expanded={open}
      >
        <span>{options.find((o) => o.value === sortOption)?.label}</span>
        <img src={open ? UpIcon : DownIcon} alt="" aria-hidden="true" />
      </button>

      {open && (
        <ul className="sort-dropdown__list" role="listbox">
          {options.map((opt, idx) => (
            <li
              key={opt.value}
              role="option"
              aria-selected={sortOption === opt.value}
              className={`sort-dropdown__option ${
                sortOption === opt.value ? "is-active" : ""
              } ${idx === options.length - 1 ? "is-last" : ""}`}
              onClick={() => {
                setSortOption(opt.value);
                setOpen(false);
              }}
            >
              {opt.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
