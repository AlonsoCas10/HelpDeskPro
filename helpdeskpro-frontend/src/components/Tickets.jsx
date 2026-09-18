

function Tickets({ tickets }) {
  return (
    <div>
      <h1>Tickets de HelpDeskPro</h1>
      {tickets.map((ticket) => (
        <div key={ticket.id}>
          <h2>{ticket.titulo}</h2>
          <p>{ticket.descripcion}</p>
          <p>Prioridad: {ticket.prioridad}</p>
          <p>Estado: {ticket.estado}</p>
          <p>Fecha: {ticket.fechaCreacion}</p>
          <hr />
        </div>
      ))}
    </div>
  )
}
export default Tickets