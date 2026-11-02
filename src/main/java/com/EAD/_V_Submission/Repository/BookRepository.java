package com.EAD._V_Submission.Repository;

import com.EAD._V_Submission.Model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
