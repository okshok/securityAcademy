import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import './index.css'
import AdminQuestion from './pages/AdminQuestion'
import AdminAnswer from './pages/AdminAnswer'
import Question from './pages/Question'
import Leaderboard from './pages/Leaderboard'
import CustomerHome from './pages/CustomerHome'
import CustomerQuestion from './pages/CustomerQuestion'
import CustomerLeaderboard from './pages/CustomerLeaderboard'

function App() {
  return (
    <BrowserRouter>
      <nav className="p-4 flex gap-4 bg-neutral-800">
        <Link to="/">문제 관리</Link>
        <Link to="/answers">정답 관리</Link>
        <Link to="/leaderboard">Admin Leaderboard</Link>
        <Link to="/customer">Customer Home</Link>
        <Link to="/customer/leaderboard">Customer Leaderboard</Link>
      </nav>
      <div className="p-6 max-w-6xl mx-auto">
        <Routes>
          <Route path="/" element={<AdminQuestion/>} />
          <Route path="/answers" element={<AdminAnswer/>} />
          <Route path="/q/:id" element={<Question/>} />
          <Route path="/leaderboard" element={<Leaderboard/>} />
          <Route path="/customer" element={<CustomerHome/>} />
          <Route path="/customer/q/:id" element={<CustomerQuestion/>} />
          <Route path="/customer/leaderboard" element={<CustomerLeaderboard/>} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

const root = createRoot(document.getElementById('root')!)
root.render(<App/>)