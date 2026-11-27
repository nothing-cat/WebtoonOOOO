import {Route,Routes,useLocation,useSearchParams,useParams} from 'react-router-dom'
import Header from './components/Header/Header.jsx'
import Footer from './components/Footer/Footer.jsx'
import Sidebar from './components/Sidebar/Sidebar.jsx'
import ScrollToTop from './components/ScrollToTop.jsx'
import Home from './pages/Home/Home.jsx'
import Admin from './pages/Admin/Admin.jsx'
import {useVisitorTracking} from './hooks/useVisitorTracking.js'
import Explore from './pages/Explore/Explore.jsx'
import WebtoonDetail from './pages/WebtoonDetail/WebtoonDetail.jsx'
import Login from './pages/auth/Login.jsx'
import Signup from './pages/auth/Signup.jsx'
import Profile from './pages/Profile/Profile.jsx'
import PublicProfile from './pages/Profile/PublicProfile.jsx'
import NotFound from './pages/NotFound/NotFound.jsx'
import {Board,PostDetail,PostEditor} from './pages/Board/Board.jsx'
import './styles/community.css'

function BoardRoute() {
  const [params] = useSearchParams()
  return <Board params={params} />
}

function PostRoute() {
  const {id} = useParams()
  return <PostDetail key={id} id={id} />
}

function EditorRoute() {
  const {id} = useParams()
  const [params] = useSearchParams()
  return <PostEditor key={id || 'new'} id={id} params={params} />
}

export default function App() {
  const {pathname} = useLocation()
  useVisitorTracking(pathname)
  const hasSidebar = pathname === '/' || pathname === '/explore'

  return (
    <div className="app">
      <Header />
      <ScrollToTop />
      <div className={hasSidebar ? 'community-shell' : 'community-full'}>
        {hasSidebar && <Sidebar />}
        <main className="community-main">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/explore" element={<Explore />} />
            <Route path="/webtoon/:id" element={<WebtoonDetail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/profile/:id" element={<PublicProfile />} />
            <Route path="/admin" element={<Admin />} />
            <Route path="/board" element={<BoardRoute />} />
            <Route path="/board/write" element={<EditorRoute />} />
            <Route path="/board/:id/edit" element={<EditorRoute />} />
            <Route path="/board/:id" element={<PostRoute />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </main>
      </div>
      <Footer />
    </div>
  )
}
