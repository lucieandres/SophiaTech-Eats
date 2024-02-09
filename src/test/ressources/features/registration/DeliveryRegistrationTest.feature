Feature: Delivery Account Operations

  Scenario Outline: Deliveryman tries to login with correct credentials
    Given there is an existing deliveryman account
    When the deliveryman try to login with name "<username>" "<name>" and password "<password>"
    Then the deliveryman should be logged in successfully

    Examples:
      | username | name  | password  |
      | user1    | name1 | password1 |
      | user2    | name2 | password2 |
      | user3    | name3 | password3 |
      | user4    | name4 | password4 |
      | user5    | name5 | password5 |
      | user6    | name6 | password6 |

  Scenario Outline: Deliveryman tries to login with wrong credentials
    Given there is an existing deliveryman account
    When the deliveryman try to login with name "<username>" "<name>" and password "<password>"
    Then the registration should fail with an error

    Examples:
      | username      | name      | password      |
      | wrongUsername | name1     | password1     |
      | user1         | wrongName | password1     |
      | user1         | name1     | wrongPassword |
