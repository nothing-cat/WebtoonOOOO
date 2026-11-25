import { Link } from "react-router-dom";

import "./WebtoonCard.css";

function WebtoonCard({ webtoon, rank }) {
  const { id, title, thumbnailUrl, platforms, storyWriter, pictureWriter, genre } = webtoon;
  const creators = storyWriter === pictureWriter
    ? storyWriter
    : `${storyWriter} · ${pictureWriter}`;
  const likeCount = webtoon.likeCount ?? 0;

  return (
    <article className="webtoon-card">
      <Link to={`/webtoon/${encodeURIComponent(id)}`}>
        <div className="webtoon-cover-wrap">
          {rank != null && (
            <span className="webtoon-rank" aria-label={`${rank}위`}>
              {rank}
            </span>
          )}
          {thumbnailUrl ? (
            <img loading="lazy" decoding="async" src={thumbnailUrl} alt={`${title} 표지`} />
          ) : (
            <div className="webtoon-placeholder">표지</div>
          )}
        </div>
        <div className="webtoon-card-body">
          <div className="card-meta">
            <span>{platforms ? platforms.join(", ") : "정보 없음"}</span>
          </div>
          <h3>{title}</h3>
          <p>글/그림: {creators}</p>
          {rank != null && (
            <p className="webtoon-like-count" aria-label={`좋아요 ${likeCount}개`}>
              ♥ {likeCount.toLocaleString("ko-KR")}
            </p>
          )}
          <div className="tag-row">장르: {genre}</div>
        </div>
      </Link>
    </article>
  );
}

export default WebtoonCard;
