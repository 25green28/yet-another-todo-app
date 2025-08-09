import './SingleTodoEntity.css'

function TodoEntity({name, completed}) {
    return (
        <div className="todoBox">
          <input type="checkbox" name="completed" defaultChecked={completed} className="completedButton"/>
          <input type="text" defaultValue={name} className="titleInputField" style={completed ? {textDecoration: 'line-through', color: 'gray'} : {}}></input>
          <button className="deleteButton">🗑️</button>
        </div>
    )
}

export default TodoEntity;