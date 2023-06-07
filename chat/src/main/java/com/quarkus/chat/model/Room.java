package com.quarkus.chat.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "room")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Room extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "room_id_seq_gen")
    @SequenceGenerator(name = "room_id_seq_gen", sequenceName = "room_id_seq", allocationSize = 1)
    private Long roomId;

    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private Set<Message> messages;

    @ElementCollection
    @CollectionTable(
            name = "rooms_members",
            joinColumns = @JoinColumn(name = "room_id")
    )
    private Set<String> members;

    public static void persistRoom(Room room) {
        persist(room);
    }

    public static Room findById(long id) {
        return (Room) find("roomId", id).firstResultOptional()
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + id + " not found"));
    }
}
