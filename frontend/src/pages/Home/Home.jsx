import {Link} from 'react-router-dom'
import {useAuth} from '../../context/AuthContext.jsx'
import {useData} from '../../hooks/useData.js'
import {fetchPopularWebtoons} from '../../services/webtoonApi.js'
import {fetchFavorites} from '../../services/userApi.js'
import {fetchPopularPosts} from '../../services/boardApi.js'
import WebtoonGrid from '../../components/Webtoon/WebtoonGrid.jsx'
import SectionTitle from '../../components/ui/SectionTitle.jsx'
import {PostList,Status} from '../shared.jsx'
import {request} from '../../services/apiClient.js'
import './Home.css'

function HomeBanner({banner}) {
  const image = <img className="home-banner-image" src={banner.imageUrl} alt={banner.altText} />
  return banner.linkUrl ? <a href={banner.linkUrl}>{image}</a> : image
}
export default function Home(){const {currentUser}=useAuth();const banner=useData(()=>request('/banner'),'home-banner');const favorites=useData(fetchFavorites,currentUser?'favorites:'+currentUser.id:null,[]),popular=useData(()=>fetchPopularWebtoons({size:4}),'popular-webtoons',[]),posts=useData(fetchPopularPosts,'popular-posts',[])
return <div className="page">{banner.data ? <HomeBanner banner={banner.data}/> : <section className="hero-section"><div><p className="eyebrow">MOODTOON</p><h1>좋아하는 웹툰을 한곳에서</h1><p>다양한 플랫폼의 웹툰을 찾고 독자들과 이야기를 나눠보세요.</p></div></section>}{currentUser&&<section><SectionTitle title="좋아하는 웹툰" eyebrow="최근 클릭한 순"><Link className="text-link" to="/profile?tab=favorites">전체 보기 →</Link></SectionTitle><Status state={favorites}><WebtoonGrid webtoons={(favorites.data||[]).slice(0,4)}/></Status></section>}<section><SectionTitle title="인기 웹툰" eyebrow="좋아요 수 기준"><Link className="text-link" to="/explore?popular=true">전체 보기 →</Link></SectionTitle><Status state={popular}><WebtoonGrid ranked webtoons={(popular.data||[]).slice(0,4)}/></Status></section><section><SectionTitle title="게시판 인기글"><Link className="text-link" to="/board">게시판 바로가기 →</Link></SectionTitle><Status state={posts}><PostList compact posts={(posts.data||[]).filter(p=>p.category!=='NOTICE').slice(0,2)}/></Status></section></div>}
