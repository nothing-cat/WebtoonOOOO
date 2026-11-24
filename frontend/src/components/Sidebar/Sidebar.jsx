import {Link} from 'react-router-dom'
import {useData} from '../../hooks/useData.js'
import {fetchGenres} from '../../services/webtoonApi.js'
import {query} from '../../services/apiClient.js'
export default function Sidebar(){const {data:genres}=useData(fetchGenres,'genres',[]);return <aside className="card community-sidebar"><h3>인기 웹툰</h3><Link to="/explore?popular=true">전체</Link>{(genres||[]).map(g=><Link key={g.id} to={'/explore'+query({genre:g.id,popular:'true'})}>{g.name}</Link>)}<hr/><h3><Link to="/board">게시판</Link></h3>{[['GENERAL','일반'],['REVIEW','리뷰'],['QUESTION','질문']].map(([k,v])=><Link key={k} to={'/board?category='+k}>{v}</Link>)}</aside>}
