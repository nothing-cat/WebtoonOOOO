import { Link } from "react-router-dom";

import "./WebtoonCard.css";

function WebtoonCard({ webtoon }) {

  
  return (
    <article className="webtoon-card">
      <Link to={`/webtoon/${encodeURIComponent(webtoon.id)}`}>
        {webtoon.image ? <img src={webtoon.image} alt={`${webtoon.title} 표지`} /> : <div className="webtoon-placeholder">표지</div>}
        <div className="webtoon-card-body">
          <div className="card-meta">
            <span>
              {webtoon.platforms
                ? webtoon.platforms.join(", ")
                : webtoon.platform}
            </span>
          </div>
          <h3>{webtoon.title}</h3>
          <p>글/그림: {webtoon.sntncWritrNm === webtoon.pictrWritrNm ? webtoon.sntncWritrNm : `${webtoon.sntncWritrNm} · ${webtoon.pictrWritrNm}`}</p>
          <div className="tag-row">
            장르: {webtoon.genre}
          </div>
        </div>
      </Link>
    </article>
  );
}

export default WebtoonCard;

