import {Link} from 'react-router-dom'

export default function ProfileLink({userId, children}) {
  return userId
    ? <Link className="profile-author-link" to={'/profile/' + encodeURIComponent(userId)}>{children}</Link>
    : children
}
