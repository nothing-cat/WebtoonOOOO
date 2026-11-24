import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { request } from "../../services/api.js";
import { useData } from "../../hooks/useData.js";
import { PostList, Status } from "../shared.jsx";
import { fetchWebtoonById } from "../../services/webtoonApi.js";
import { useAuth } from "../../context/AuthContext.jsx";
import CommentList from "../../components/Comment/CommentList.jsx";
import "./WebtoonDetail.css";



function WebtoonDetail() {
  
  const { id } = useParams();

  // 로그인한 사용자 정보와 프로필 수정 함수 가져오기
  const { currentUser } = useAuth();
  const reviews = useData("/webtoons/"+encodeURIComponent(id)+"/reviews", []);

  // 웹툰 정보를 저장할 상태
  const [webtoon, setWebtoon] = useState(null);

  // 로딩 상태 저장
  const [loading, setLoading] = useState(true);
  
  // 웹툰 설명 자세히보기 상태
  const [isExpanded, setIsExpanded] = useState(false);

  useEffect(() => {
    async function load() {
      setLoading(true);



      const data = await fetchWebtoonById(id).catch(() => null);



      setWebtoon(data);
      setLoading(false);
    }

    load();
  }, [id]); // id가 바뀌면 다시 실행

  // 로딩 중일 때 화면
  if (loading) {
    return <div>로딩 중...</div>;
  }

  // 웹툰을 찾지 못했을 때
  if (!webtoon) {
    return (
      <div className="page empty-page">
        <h1>작품을 찾을 수 없습니다.</h1>
        <Link className="primary-button" to="/explore">
          탐색으로 이동
        </Link>
      </div>
    );
  }

  // 현재 웹툰이 북마크되어 있는지 확인
  const isBookmarked = Boolean(webtoon.isFavorite);
  const handleBookmark = async () => { if(!currentUser){alert("로그인 후 이용해주세요.");return;} try{await request("/webtoons/"+encodeURIComponent(webtoon.id)+"/favorite",isBookmarked?"DELETE":"POST");setWebtoon(await fetchWebtoonById(id));}catch(e){alert(e.message);} };
  return (
    <div className="page">
      <section className="detail-layout">
        {webtoon.image ? <img className="detail-cover" src={webtoon.image} alt={`${webtoon.title} 표지`} /> : <div className="detail-cover webtoon-placeholder">표지</div>}
        <div className="card detail-info">
          <div className="detail-topbar">
            <p className="eyebrow">
              {webtoon.platforms?.length
                ? webtoon.platforms.join(", ")
                : webtoon.platform}
            </p>
            <button className="bookmark-button" onClick={handleBookmark}>
              {isBookmarked ? "♥ 좋아하는 작품에서 해제" : "♡ 좋아하는 작품에 추가"}
            </button>
          </div>
          <h1>{webtoon.title}</h1>
          <div className="description-wrap">
            <p className="description">
              {isExpanded
                ? webtoon.description
                : webtoon.description?.slice(0, 120)}
              {!isExpanded && webtoon.description?.length > 120 && "..."}
            </p>

            {webtoon.description?.length > 120 && (
              <button
                className="more-button"
                onClick={() => setIsExpanded((prev) => !prev)}
              >
                {isExpanded ? "접기" : "더보기"}
              </button>
            )}
          </div>

          <dl className="info-list">
            <div>
              <dt>글/그림 작가</dt>
              <dd>
                {webtoon.sntncWritrNm === webtoon.pictrWritrNm
                  ? webtoon.sntncWritrNm
                  : `${webtoon.sntncWritrNm} · ${webtoon.pictrWritrNm}`}
              </dd>
            </div>
            <div>
              <dt>장르</dt>
              <dd>{webtoon.genre}</dd>
            </div>
            <div>
              <dt>연령등급</dt>
              <dd>{webtoon.ageGrade || "정보 없음"}</dd>
            </div>
          </dl>
        </div>
      </section>

      <CommentList key={webtoon.id} webtoonId={webtoon.id} />
      <section className="card community-panel"><h2>관련 리뷰</h2><Status state={reviews}><PostList posts={reviews.data||[]}/></Status>{currentUser&&<Link className="text-link" to={"/board/write?category=REVIEW&webtoonId="+encodeURIComponent(webtoon.id)}>리뷰 작성 →</Link>}</section>
    </div>
  );
}

export default WebtoonDetail;

