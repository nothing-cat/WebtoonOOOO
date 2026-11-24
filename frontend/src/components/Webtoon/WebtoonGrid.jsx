import WebtoonCard from "./WebtoonCard.jsx";
import "./WebtoonGrid.css";

function WebtoonGrid({ webtoons, ranked = false }) {
  if (webtoons.length === 0) {
    return <p className="empty-message">{ranked ? "아직 인기 웹툰이 없습니다." : "표시할 웹툰이 없습니다."}</p>;
  }

  return (
    <div className="webtoon-grid">
      {webtoons.map((webtoon, index) => (
        <WebtoonCard key={webtoon.id} webtoon={webtoon} rank={ranked ? index + 1 : undefined} />
      ))}
    </div>
  );
}

export default WebtoonGrid;
