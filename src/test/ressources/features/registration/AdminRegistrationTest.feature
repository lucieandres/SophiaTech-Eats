Feature: Admin Login

  Scenario: Admin tries to login with correct credentials
    Given there is an existing admin account
    When the admin try to login with name "admin" "admin" and password "admin"
    Then the admin should be logged in successfully

  Scenario Outline: Admin tries to login with wrong credentials
    Given there is an existing admin account
    When the admin try to login with name "<username>" "<name>" and password "<password>"
    Then the registration should fail with an error

    Examples:
      | username      | name      | password      |
      | wrongUsername | admin     | admin     |
      | admin         | wrongName | admin     |
      | admin         | admin     | wrongPassword |