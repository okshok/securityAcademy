import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '../lib/api'

export default function CustomerQuestion() {
  const { id } = useParams()
  const [q, setQ] = useState<any>(null)
  const [choice, setChoice] = useState<string>('O')
  useEffect(() => {
    api.get('/customer/questions/today').then(r => {
      const found = r.data.find((it:any)=> String(it.id)===String(id))
      setQ(found)
    })
  }, [id])
  const submit = async () => {
    await api.post('/customer/predictions', { questionId: Number(id), choice })
    alert('제출 완료. 토론방 링크: https://intra.example.com/board')
  }
  if (!q) return <div>로딩...</div>
  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">{q.prompt}</h1>
      <div className="grid grid-cols-2 gap-4">
        <div className="p-3 rounded-lg bg-green-900/20">
          <div className="font-semibold mb-2">상승 근거</div>
          <ul className="list-disc ml-5">
            {q.pros?.map((p:any)=> <li key={p.id}><a href={p.sourceUrl||'#'} target="_blank">{p.text}</a></li>)}
          </ul>
        </div>
        <div className="p-3 rounded-lg bg-red-900/20">
          <div className="font-semibold mb-2">하락 근거</div>
          <ul className="list-disc ml-5">
            {q.cons?.map((c:any)=> <li key={c.id}><a href={c.sourceUrl||'#'} target="_blank">{c.text}</a></li>)}
          </ul>
        </div>
      </div>
      <div className="flex gap-3 items-center">
        <label><input type="radio" name="choice" checked={choice==='O'} onChange={()=>setChoice('O')}/> O</label>
        <label><input type="radio" name="choice" checked={choice==='X'} onChange={()=>setChoice('X')}/> X</label>
        <button onClick={submit} className="px-3 py-1 rounded-lg bg-neutral-700">제출</button>
      </div>
    </div>
  )
}
