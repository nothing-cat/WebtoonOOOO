import { Link } from "react-router-dom";

import "./WebtoonCard.css";

function WebtoonCard({ webtoon }) {

  
  return (
    <article className="webtoon-card">
      <Link to={`/webtoon/${encodeURIComponent(webtoon.id)}`}>
        {webtoon.thumbnailUrl ? <img src={webtoon.thumbnailUrl} alt={`${webtoon.title} 표지`} /> : <div className="webtoon-placeholder">표지</div>}
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
          <div className="tag-row">
            장르: {webtoon.genre}
          </div>
        </div>
      </Link>
    </article>
  );
}

export default WebtoonCard;

