## 무엇을 하는가?
[Hoisting state in composable objects](https://fvilarino.medium.com/hoisting-state-in-composable-objects-a833195752c4)이 사이트에 나온 것을 따라하면서 jetpack compose에서 사용하는 Hoisitng state라는 기법(?)을 경험해보자.

## 해당 사이트의 번역
<h4>Background</h4>
Jetpack compose의 원칙들 중 하나는 state(상태)를 `hoist`한다는 것이다. 
즉, a composable's state를 composable의 외부로 움직이고, 그것을 멀리 밀어넣는 것이다. 이것은 the composable stateless를 만든다.
statkess composables를 만드는 것은 재사용하고 테스트하기 더 쉬운 컴포넌트들을 결과로 도출한다.<br>

이 기사에서, 우리는 자신의 상태를 `hoist`하는 composable을 어떻게 만들 수 있는 지 볼 것이다. 
이와 동시에 기본값들이 우리의 필요를 만족시킬 때, 사용되어지는 기본 상태 구현을 제공할 것이다.

## 요구사항
우리가 달성하고자 하는 것은 그 이메일 주소가 타당하지 않을 때 오류 라벨을 보여주는 email input field를 만드는 것이다.
<img src = "https://miro.medium.com/v2/resize:fit:640/format:webp/1*HxlVQP18VwOCMM0-0i3-3g.png">
text가 유효하면(여기서는 empty 값도 유효한 것으로 처리할 것이다.), 오류 label은 보이지 않고, 그렇지 않으면 보일 것이다.
이 기사에서, 우리는 우리의 composable위에 옵션들을 간단하게하고 최소화해서 진행할 것이다.

## 내부 상태를 가진 최초의 구현
바로 떠오른느 첫 번째 해결책은 이 composable을 이메일 유효성을 다루는 자신의 내부 상태를 가진 채로 구현 하는 것이다.
그 이유는 네가 여러 장소에서 이러한 composable을 사용하길 원할 때, 너는 그 composable이 사용될 때마다 유효성 검사를 구현하길 원하지 않기 때문이다.
그래서 그 상태(state)를 직접적으로 composable로 추가하는 것은 괜찮은 접근처럼 보인다. (이것은 괜찮은 접근이 아니다.)
이제, 어떻게 우리가 그러한 내부 상태를 가진 composable을 구현하는지 보자.

```kotlin
@Composable
fun EmailInputField(
    text: String,
    onValueChange: (String) -> Unit,
    isValid: Boolean,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label,
            isError = !isValid,
        )
        if (!isValid) {
            Text(
                text = "Invalid email",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error,
            )
        }
    }
}
```

우리는 composable에서 그 state를 지웠다. 그리고 우리는 caller에게 그 책임을 전가하려고 한다. 이러한 composable은 오직 그 내용을 보여주고, input field의 update를 caller에게 알리는 것에 책임이 있다.
상태의 어떤 변화(email field가 변화하거나 그것의 타당성이 변화하거나)는 이제 이 composable 외부에 있다.<br>

우리가 그 컴포저블을 호출할 때, 우리는 그 상테와 유효성을 제공할 필요가 있다. 이 컴포저블을 사용하기 위해서, 우리는 아래와 같은 것을 해야한다.

```kotlin
var email by remember { mutableStateOf("") }
var isValid by remember { mutableStateOf(true) }
EmailInputField(
    text = email,
    onValueChange = { value ->
        email = value
        isValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(value).matches()
    },
    isValid = isValid,
    modifier = Modifier.padding(all = 16.dp),
    label = { Text(text = "Email address") },
)
```
이제 완성된 ctronl과 vissibility를 가졌다. email field의 내용과 validation status에 대해서 완성된 control과 visibility를 가졌다.;
이것은 composable이 훨씬 더 유연하게 한다. 예를 들어 다른 시나리오에서 다른 validation 전략을 실행할 수 있다. 우리의 callback은 사용자가 email field를 업데이트 할 때마다 알림 받고 각 업데이트에 대해서, 우리는 email이 valid한지 계산한다.
그 업데이트된 이메일 값과 유효성 값은 composable에서 email field를 업데이트하고 Invalid field를 숨기는데 사용된다.<br>

말하자면, 이 솔루션은 완벽하지 않다. 우리가 이 컴포저블을 사용하기 원하는 다양한 장소가 있다면, 우리는 모든 장소에서 그 state를 다룰 필요가 있다. 잠재적으로 같은 로직을 우리의 app에 복제하면서.<br>

이것을 다루기 위한 더 나은 방식이 있는가?
대부분의 경우에 우리가 이용할 수 있는 몇 기본 방법을 가지는 동시에 + 그 state가 필요할 때, 사용자 지정(커스텀)을 위해 우리의 state를 끌어 올리는 방법을 보자. 


## 기본 상태 끌어올리기 (Default state hoisting)
첫 단계는 `interface`를 정의하는 것이다. 우리의 경우, 우리의 state를 정의하는 2개의 item(`email`과 `isValid`)이 있다.
```kotlin
@Stable
interface EmailState {
    var email: String
    val isValid: Boolean
}
```
Jetpack Compose의 컨벤션을 사용하는 것에 주목해라. 이 상태 인터페이스는 `State`를 가진 컴포저블이 붙고 인터페이스의 이름 끝에 컴포저블 이름이 이어져 붙어있다.<br>

다음 단계로 우리는 이 인터페이스의 구현(implementation)을 만들 것이다. 이때, 이 인터페이스는 우리가 커스텀할 필요가 없는 모든 케이스에 대해 기본 유효성을 제공하는 것이다.
```kotlin
class EmailInputFieldStateImpl(
    email: String = "",
) : EmailInputFieldState {

    // 1
    override var email: String by mutableStateOf(email)
        private set

    // 2    
    override val isValid by derivedStateOf { isValidEmail(_email) }

    // 3
    private fun isValidEmail(email: String): Boolean =
        email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()

    companion object {
        // 4
        val Saver = Saver<EmailInputFieldStateImpl, List<Any>>(
            save = { listOf(it._text) },
            restore = {
                EmailInputFieldStateImpl(
                    email = it[0] as String,
                )
            }
        )
    }
}

@Composable
fun rememberEmailInputFieldState(): EmailInputFieldState = rememberSaveable(
    saver = EmailInputFieldStateImpl.Saver
) {
    EmailInputFieldStateImpl("")
}
```
1. 우리는 email value를 담는 a private `mutableStateOf`를 정의한다. 이것은 우리가 생성자에서 받는 값으로 초기화 된다. 생성자의 기본 값은 empty string이다. 우리는 email value를 get, set하는 a public property를 노출시킨다; 그 email value는 observalbe 해진다.
2. `isValid` 필드를 위한 두 번째 `mutableStateOf`를 정의한다. 이 값이 이메일 필드로부터 자극 받아서, 컴포즈로부터 기존의 `derivedStateOf`를 이용한다. 이 값 역시 마찬가지로 observable이다.
3. 이것은 우리의 표준 이메일 검증 로직이다.
4. 우리는 a saver를 제공한다. 그 결과 activity나 fragment가 우리의 composable이 재 생성되는 것을 주관할 때 그 state는 저장되어지고 회복 된다.
5. 그 state는 기억되어질 필요가 있다. 그래서 우리는 하나의 utility function을 가진다. 그 utility function은 우리의 상태를 기억하고 그 saver가 그것의 내용을 보존하게 한다. 즉, 다음의 표준 젯펙 컴포즈 컨벤션에서, 이러한 메소드는 `remember`이란 이름으로 시작한다.

다음 단계로, 우리는 우리의 email composable을 다시 작서애서 이 state를 수용하고 그것의 값을 사용해서 UI를 drive할 것이다.
다른 말로, 우리는 email value, valid flag, state의 callback을 대체할 것이다. 일단 우리가 우리의 composable을 refactor하면, 그것은 아래와 같다.

```kotlin
@Composable
fun EmailInputField(
    modifier: Modifier,
    state : EmailInputFieldState = rememberEmailInputFieldState(),
    label : @Composable (()->Unit)? = null,
){
    Column(modifier = modifier){
        OutlinedTextField(
            value = state.email,
            onValueChange = { value -> state.email = value },
            modifier = Modifier.fillMaxWidth(),
            label = label,
            isError = !state.isValid,
        )
        if (!state.isValid) {
            Text(
                text = "Invalid email",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error,
            )
        }
    }
}
```
우리는 `EmailInputFieldState` 타입의 `state`를 받아들이고 그것을 우리의 표준 구현의 기본으로하고 있다.
이제, 사용자가 email input field를 업뎃할 때, 우리는 그 상태의 email field를 업뎃한다. 그리고 이것은 email의 유효성 계산을 유발하고 그리고 나서 error field가 표시되는지 아닌지를 결정한다.<br>

이러한 해결책으로, 우리는 각 call site의 logic을 처리하지 않아도 된다. 만약 기본 이메일 유효성이 우리에게 작동한다면, 우리는 그 기본 상태가 우리의 input을 다루게 한다.<br>

이 위젯을 사용하기 위해, 우리는 우리의 composition tree에 아래와 같이 그것을 추가한다.
```kotlin
EmailInputField(
    modifier = Modifier
        .fillMaxWidth()
        .padding(all = 16.dp),
    label = { Text(text = "Email address") },
)
```
만약 우리가 우리의 custom email validation을 갖고 싶다면, 우리는 EmailInputFieldState의 구현을 만들고 그것을 composable로 제공하면 된다.<br>

이러한 구현과 함께, 우리는 그 email field 값과 그것의 유효성을 관찰할 수 있다. 기분 상태 구현을 사용하면서, 우리는 그저 composable 외부에서 그 상태를 초기화하고 그것을 생성자로 제공해줄 필요가 있다.
아래의 정보 한토막(snippet)은 어떻게 해야하는 지 보옂주고, 현재 이메일 주소가 무엇이고 그것이 타당한지를 나타내주는 email composable 아래의 textfield를 보여준다.
```kotlin
val state = rememberEmailInputFieldState()

EmailInputField(
    modifier = Modifier
        .fillMaxWidth()
        .padding(all = 16.dp),
    state = state,
    label = { Text(text = "Email address") },
)

Text(
    text = "Email [${state.email}] is ${(if (state.isValid) "valid" else "invalid")}",
    modifier = Modifier.padding(all = 16.dp),
    style = MaterialTheme.typography.body1,
)
```
우리는 제공된 `rememberEmailInputFieldState` 메소드를 사용해서 그 상태의 객체를 얻고, 이 상태를 composable로 제공해서 그것을 현재 값과 유효성을 보여주는데 사용한다.<br>

이러한 솔루션으로 우리는 두 세게의 장점을 모두 가진다. 
우리는 있는 그대로 사용할 수 있고, 
상태 자체를 처리하는 동시에 상태를 관찰할 수 있는 컴포저블을 가진다.
그리고 그 컴포저블은 호출자가 그들 자신의 상태 구현을 제공하는 것을 허락함으로써 커스텀화된 선택을 제공할 수 있다.