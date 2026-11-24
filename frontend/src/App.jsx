import {AuthProvider} from './context/AuthContext.jsx'
import Layout from './components/Layout.jsx'
import {useRoute} from './router.js'
import {Home,Explore,WebtoonDetail} from './pages/Webtoons.jsx'
import {Board,PostDetail,PostEditor} from './pages/Board.jsx'
import {Account,Profile} from './pages/Account.jsx'
function Routes(){const {path,params,route}=useRoute();let page;const parts=path.split('/').filter(Boolean)
if(path==='/')page=<Home/>
else if(path==='/explore')page=<Explore params={params}/>
else if(parts[0]==='webtoon'&&parts.length===2)page=<WebtoonDetail key={route} id={decodeURIComponent(parts[1])}/>
else if(path==='/board')page=<Board params={params}/>
else if(path==='/board/write')page=<PostEditor key={route} params={params}/>
else if(parts[0]==='board'&&/^\d+$/.test(parts[1]||'')&&parts.length===3&&parts[2]==='edit')page=<PostEditor key={route} id={parts[1]} params={params}/>
else if(parts[0]==='board'&&/^\d+$/.test(parts[1]||'')&&parts.length===2)page=<PostDetail key={route} id={parts[1]}/>
else if(path==='/login'||path==='/signup')page=<Account key={path} signup={path==='/signup'}/>
else if(path==='/profile')page=<Profile/>
else page=<><h1>페이지를 찾을 수 없습니다.</h1><a href="#/">홈으로</a></>
return <Layout>{page}</Layout>}
export default function App(){return <AuthProvider><Routes/></AuthProvider>}
