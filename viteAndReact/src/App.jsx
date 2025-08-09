import './App.css'
import {useState} from "react";
import SingleTodoEntity from "./SingleTodoEntity.jsx";

function App() {
  const [todos, setTodos] = useState([
    {
      "id": 1,
      "title": "My first todo",
      "completed": true,
      "modificationDate": "2025-08-08T17:02:45.280898",
      "creationDate": "2025-08-08T17:02:45.280898"
    },
    {
      "id": 2,
      "title": "Clean my car",
      "completed": false,
      "modificationDate": "2025-08-08T17:02:53.905707",
      "creationDate": "2025-08-08T17:02:53.905707"
    },
    {
      "id": 3,
      "title": "Do dishes",
      "completed": true,
      "modificationDate": "2025-08-08T17:03:03.0802",
      "creationDate": "2025-08-08T17:03:03.0802"
    },
    {
      "id": 4,
      "title": "Go to sleep",
      "completed": false,
      "modificationDate": "2025-08-08T17:03:10.05977",
      "creationDate": "2025-08-08T17:03:10.05977"
    }
  ])
  return (
      <div className="mainContainer">
        <h1 className="applicationName">Yet another todo app</h1>
        <div className="newTodoContainer">
            <input className="inputField" placeholder="Enter new todo name"/>
            <button onClick={console.log("Add new todo")} className="addTodoButton">Add</button>
        </div>
        <div className="todosContainer">
          {todos.map(item =>
            <SingleTodoEntity key={item.id} name={item.title} completed={item.completed}/>
          )}
        </div>
      </div>
  )
}

export default App
