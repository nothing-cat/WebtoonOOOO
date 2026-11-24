import './WebtoonCard.css'
export default function WebtoonCard({webtoon:w}){
 const authors=[...new Set([w.storyWriter,w.pictureWriter].filter(Boolean))].join(' · ')
 return <article className="webtoon-card"><a href={'#/webtoon/'+encodeURIComponent(w.id)}>
 {w.thumbnailUrl?<img src={w.thumbnailUrl} alt={w.title+' 표지'}/>:<div className="cover">표지</div>}
 <div className="webtoon-card-body"><h3>{w.title}</h3><p>{authors||'작가 정보 없음'}</p></div>
 </a></article>
}
