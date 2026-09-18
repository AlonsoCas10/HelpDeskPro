package com.helpdeskpro.helpdeskpro_backend.controller;

import com.helpdeskpro.helpdeskpro_backend.model.Ticket;
import com.helpdeskpro.helpdeskpro_backend.repository.TicketRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public List<Ticket> listarTickets() {
        return ticketRepository.findAll();
    }

    @PostMapping
    public Ticket crearTicket(@RequestBody Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @GetMapping("/{id}")
    public Ticket buscarTicket(@PathVariable Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Ticket actualizarTicket(
            @PathVariable Long id,
            @RequestBody Ticket ticketActualizado) {

        Ticket ticket = ticketRepository.findById(id).orElse(null);

        if (ticket == null) {
            return null;
        }

        ticket.setTitulo(ticketActualizado.getTitulo());
        ticket.setDescripcion(ticketActualizado.getDescripcion());
        ticket.setPrioridad(ticketActualizado.getPrioridad());
        ticket.setEstado(ticketActualizado.getEstado());
        ticket.setFechaCreacion(ticketActualizado.getFechaCreacion());

        return ticketRepository.save(ticket);
    }

    @DeleteMapping("/{id}")
    public void eliminarTicket(@PathVariable Long id) {
        ticketRepository.deleteById(id);
    }
}