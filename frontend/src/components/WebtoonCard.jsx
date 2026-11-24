import './WebtoonCard.css'
export default function WebtoonCard({webtoon:w}){
 return <article className="webtoon-card"><a href={'#/webtoon/'+encodeURIComponent(w.id)}>
 {w.thumbnailUrl?<img src={w.thumbnailUrl} alt={w.title+' 표지'}/>:<div className="cover" style={{background:w.color||'#64748b'}}>{w.title}</div>}
 <div className="webtoon-card-body"><div className="card-meta">{w.platforms?.join(', ')}</div><h3>{w.title}</h3><p>글/그림: {w.storyWriter===w.pictureWriter?w.pictureWriter:[w.storyWriter,w.pictureWriter].filter(Boolean).join(' · ')}</p><div className="tag-row">{w.genre||'장르 미상'} · ♥ {w.likeCount||0}</div></div></a></article>
}
