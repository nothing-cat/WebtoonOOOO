import { Link } from "react-router-dom";

import "./WebtoonCard.css";

function WebtoonCard({ webtoon, rank }) {

  
  return (
    <article className="webtoon-card">
      <Link to={`/webtoon/${encodeURIComponent(webtoon.id)}`}>
        <div className="webtoon-cover-wrap">
          {rank != null && <span className="webtoon-rank" aria-label={`${rank}위`}>{rank}</span>}
        {webtoon.thumbnailUrl ? <img loading="lazy" decoding="async" src={webtoon.thumbnailUrl} alt={`${webtoon.title} 표지`} /> : <div className="webtoon-placeholder">표지</div>}
        </div>
        <div className="webtoon-card-body">
          <div className="card-meta">
            <span>
              {webtoon.platforms
                ? webtoon.platforms.join(", ")
                : '정보 없음'}
            </span>
          </div>
          <h3>{webtoon.title}</h3>
          <p>글/그림: {webtoon.storyWriter === webtoon.pictureWriter ? webtoon.storyWriter : `${webtoon.storyWriter} · ${webtoon.pictureWriter}`}</p>
          {rank != null && <p className="webtoon-like-count" aria-label={`좋아요 ${webtoon.likeCount ?? 0}개`}>♥ {(webtoon.likeCount ?? 0).toLocaleString("ko-KR")}</p>}
          <div className="tag-row">
            장르: {webtoon.genre}
          </div>
        </div>
      </Link>
    </article>
  );
}

export default WebtoonCard;

