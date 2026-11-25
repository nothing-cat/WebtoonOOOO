import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import * as api from '../services/commentApi.js'

export function useComments(webtoonId) {
  const { currentUser } = useAuth()
  const [commentList, setCommentList] = useState([])
  const [isLoading, setLoading] = useState(true)
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let active = true
    setLoading(true)

    api.fetchComments(webtoonId)
      .then(comments => {
        if (active) setCommentList(comments)
      })
      .catch(error => {
        if (active) alert(error.message)
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [webtoonId, version, currentUser?.id])

  async function runCommentAction(action) {
    if (!currentUser) {
      alert('로그인 후 이용해 주세요.')
      return false
    }

    try {
      await action()
      setVersion(previousVersion => previousVersion + 1)
      return true
    } catch (error) {
      alert(error.message)
      return false
    }
  }

  return {
    commentList,
    isLoading,
    addComment: text => runCommentAction(() => api.createComment(webtoonId, text)),
    deleteComment: id => runCommentAction(() => api.deleteComment(id)),
    editComment: (id, text) => runCommentAction(() => api.updateComment(id, text)),
    toggleEmpathy: id => runCommentAction(() => {
      const comment = commentList.find(comment => comment.id === id)
      return api.setCommentLike(id, !comment?.isLiked)
    }),
  }
}
