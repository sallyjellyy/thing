package so.kciter.thing

import org.junit.Test
import so.kciter.thing.normalizer.trim
import so.kciter.thing.redactor.creditCard
import so.kciter.thing.validator.ValidationResult
import so.kciter.thing.validator.email
import so.kciter.thing.validator.emoji
import so.kciter.thing.validator.maximum
import so.kciter.thing.validator.minimum
import so.kciter.thing.validator.notEmpty
import kotlin.test.assertEquals

class ThingTest {
  @Test
  fun normalizeTest() {
    val person = Person(" kciter ", " kciter@naver.com ", " 1234-1234-1234-1234 ", 100, "\uD83D\uDE06")
    person.normalize()

    assertEquals(person.username, "kciter")
    assertEquals(person.email, "kciter@naver.com")
    assertEquals(person.creditCard, "1234-1234-1234-1234")
  }

  @Test
  fun validateTest() {
    val person1 = Person("", "kciter@naver.com", "1234-1234-1234-1234", 100, "emoji")
    val result1 = person1.validate()
    assert(result1 is ValidationResult.Invalid)
    assertEquals(result1.errors.size, 3)
    assertEquals(result1.errors[0].dataPath, ".username")
    assertEquals(result1.errors[0].message, "must not be empty")
    assertEquals(result1.errors[1].dataPath, ".age")
    assertEquals(result1.errors[1].message, "must be at most '70'")
    assertEquals(result1.errors[2].dataPath, ".emoji")
    assertEquals(result1.errors[2].message, "must be a valid emoji unicode set")

    val person2 = Person("kciter", "kciter@naver.com", "1234-1234-1234-1234", 50, "\uD83D\uDE06")
    val result2 = person2.validate()
    assert(result2 is ValidationResult.Valid)
  }

  @Test
  fun redactTest() {
    val person = Person("kciter", "kciter@naver.com", "1234-1234-1234-1234", 50, "\uD83D\uDE06")
    person.redact()
    assertEquals(person.creditCard, "[REDACTED]")
  }

  data class Person(
    val username: String,
    val email: String,
    val creditCard: String,
    val age: Int,
    val emoji: String
  ): Thing<Person> {
    override val rule: Rule<Person>
      get() = Rule {
        Normalization {
          Person::username { trim() }
          Person::email { trim() }
          Person::creditCard { trim() }
        }

        Validation {
          Person::username{ notEmpty() }
          Person::email { email() }
          Person::age {
            minimum(10)
            maximum(70)
          }
          Person::emoji { emoji() }
        }

        Redaction {
          Person::creditCard { creditCard() }
        }
      }
  }
}
