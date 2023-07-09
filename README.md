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