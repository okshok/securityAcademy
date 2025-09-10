import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'

export default function CustomerHome() {
  const [items, setItems] = useState<any[]>([])
  useEffect(() => {
    api.get('/customer/questions/today').then(r => setItems(r.data))
  }, [])
  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">오늘의 문제</h1>
      {items.map(it => (
        <div key={it.id} className="p-4 rounded-xl bg-neutral-800">
          <div className="font-semibold">{it.prompt}</div>
          <div className="text-sm opacity-80">마감: {new Date(it.closesAt).toLocaleString()}</div>
          <Link to={`/customer/q/${it.id}`} className="inline-block mt-3 px-3 py-1 rounded-lg bg-neutral-700">자세히</Link>
        </div>
      ))}
    </div>
  )
}
1