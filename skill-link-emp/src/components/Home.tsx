import { mockPosts } from "../data/mockData";
import { PostCard } from "./PostCard";


export const Home = ()=>{
  return(
    <main className="min-h-screen bg-gradient-to-br from-emerald-500 via-teal-600 to-purple-700">
      <h1>Home Page</h1>
      {
        mockPosts.map((post)=>(
          <PostCard key={post.id} post={post} />
        ))
      }
    </main>
  )
}